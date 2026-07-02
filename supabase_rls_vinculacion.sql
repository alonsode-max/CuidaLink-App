-- ============================================================================
-- CuidaLink · Políticas RLS para login, perfiles y vinculación paciente↔cuidador
-- ----------------------------------------------------------------------------
-- Ejecutar en: Supabase Dashboard → SQL Editor → New query → pegar todo → Run.
-- Es idempotente: se puede ejecutar varias veces sin romper nada.
--
-- Problema que arregla: con RLS activado y sin estas políticas, NI EL DUEÑO
-- puede leer su propia fila, así que:
--   · el paciente no puede guardar/leer su código,
--   · el cuidador no encuentra al paciente por código ("no encontramos paciente").
-- ============================================================================

-- ============ PATIENTS ============
alter table patients enable row level security;

-- El cuidador necesita poder buscar al paciente por su código de 6 dígitos.
drop policy if exists patients_select_auth on patients;
create policy patients_select_auth on patients
  for select to authenticated using (true);

-- El paciente edita SOLO su propia fila (p. ej. guardar su código).
drop policy if exists patients_update_own on patients;
create policy patients_update_own on patients
  for update to authenticated using (auth.uid() = uid) with check (auth.uid() = uid);

-- Alta del paciente (el insert manual de la app; el trigger ya crea la fila).
drop policy if exists patients_insert_own on patients;
create policy patients_insert_own on patients
  for insert to authenticated with check (auth.uid() = uid);

-- El CUIDADOR VINCULADO puede actualizar la fila de SU paciente. Sin esta
-- política, RLS bloqueaba en silencio el SOS (sos_alert_trigger), el guardado de
-- la zona segura (geofence_*) y la petición de ubicación: el UPDATE afectaba 0
-- filas pero PostgREST devolvía 200, así que el paciente nunca recibía el cambio.
-- (Postgres combina las políticas UPDATE con OR: paciente-dueño O cuidador-vinculado.)
drop policy if exists patients_update_linked_caretaker on patients;
create policy patients_update_linked_caretaker on patients
  for update to authenticated
  using (
    id in (
      select patient_id from vinculations
      where caretaker_id in (select id from caretakers where uid = auth.uid())
    )
  )
  with check (
    id in (
      select patient_id from vinculations
      where caretaker_id in (select id from caretakers where uid = auth.uid())
    )
  );

-- ============ CARETAKERS ============
alter table caretakers enable row level security;

drop policy if exists caretakers_select_auth on caretakers;
create policy caretakers_select_auth on caretakers
  for select to authenticated using (true);

drop policy if exists caretakers_update_own on caretakers;
create policy caretakers_update_own on caretakers
  for update to authenticated using (auth.uid() = uid) with check (auth.uid() = uid);

drop policy if exists caretakers_insert_own on caretakers;
create policy caretakers_insert_own on caretakers
  for insert to authenticated with check (auth.uid() = uid);

-- ============ VINCULATIONS ============
alter table vinculations enable row level security;

-- El cuidador crea la vinculación, pero SOLO con su propio caretaker_id.
drop policy if exists vinculations_insert_caretaker on vinculations;
create policy vinculations_insert_caretaker on vinculations
  for insert to authenticated
  with check (caretaker_id in (select id from caretakers where uid = auth.uid()));

-- Cada parte ve solo sus propias vinculaciones (sondeo "ya vinculado").
drop policy if exists vinculations_select_own on vinculations;
create policy vinculations_select_own on vinculations
  for select to authenticated using (
    caretaker_id in (select id from caretakers where uid = auth.uid())
    or patient_id in (select id from patients where uid = auth.uid())
  );

-- Cualquiera de las dos partes (paciente o cuidador) puede romper SU vínculo.
-- Sin esta política RLS bloquea el DELETE y "Desvincular" no borra nada.
drop policy if exists vinculations_delete_own on vinculations;
create policy vinculations_delete_own on vinculations
  for delete to authenticated using (
    caretaker_id in (select id from caretakers where uid = auth.uid())
    or patient_id in (select id from patients where uid = auth.uid())
  );

-- ============================================================================
-- (Opcional) Verificar las políticas creadas:
--   select tablename, policyname, cmd
--   from pg_policies
--   where tablename in ('patients','caretakers','vinculations')
--   order by tablename, policyname;
-- ============================================================================
