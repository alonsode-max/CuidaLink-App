-- ============================================================================
-- CuidaLink · Calendario compartido: índice de upsert + RLS de events / event_completions
-- ----------------------------------------------------------------------------
-- Ejecutar en: Supabase Dashboard → SQL Editor → New query → pegar todo → Run.
-- Es idempotente: se puede ejecutar varias veces sin romper nada.
--
-- Arregla dos cosas que impedían que "marcar como completada" funcionara:
--   1) El upsert usa ON CONFLICT (event_id, date); sin un índice ÚNICO sobre esas
--      columnas Postgres lanza y la marca no se guardaba (el error se tragaba).
--   2) Con RLS activado y sin políticas, el paciente no podía INSERTAR el
--      completado y el cuidador no podía LEERLO → no aparecía ni en su calendario
--      ni en el historial.
-- ============================================================================

-- ---------- Índice único para el upsert de completados ----------
-- (event_id + date) identifica un completado; permite ON CONFLICT (event_id,date).
create unique index if not exists event_completions_event_date_uidx
  on event_completions (event_id, date);

-- ============ EVENTS ============
alter table events enable row level security;

drop policy if exists events_select on events;
create policy events_select on events
  for select to authenticated using (
    patient_id in (select id from patients where uid = auth.uid())
    or patient_id in (
      select v.patient_id from vinculations v
      join caretakers c on c.id = v.caretaker_id
      where c.uid = auth.uid()
    )
  );

drop policy if exists events_insert on events;
create policy events_insert on events
  for insert to authenticated with check (
    patient_id in (select id from patients where uid = auth.uid())
    or patient_id in (
      select v.patient_id from vinculations v
      join caretakers c on c.id = v.caretaker_id
      where c.uid = auth.uid()
    )
  );

drop policy if exists events_update on events;
create policy events_update on events
  for update to authenticated using (
    patient_id in (select id from patients where uid = auth.uid())
    or patient_id in (
      select v.patient_id from vinculations v
      join caretakers c on c.id = v.caretaker_id
      where c.uid = auth.uid()
    )
  ) with check (
    patient_id in (select id from patients where uid = auth.uid())
    or patient_id in (
      select v.patient_id from vinculations v
      join caretakers c on c.id = v.caretaker_id
      where c.uid = auth.uid()
    )
  );

drop policy if exists events_delete on events;
create policy events_delete on events
  for delete to authenticated using (
    patient_id in (select id from patients where uid = auth.uid())
    or patient_id in (
      select v.patient_id from vinculations v
      join caretakers c on c.id = v.caretaker_id
      where c.uid = auth.uid()
    )
  );

-- ============ EVENT_COMPLETIONS ============
alter table event_completions enable row level security;

drop policy if exists event_completions_select on event_completions;
create policy event_completions_select on event_completions
  for select to authenticated using (
    patient_id in (select id from patients where uid = auth.uid())
    or patient_id in (
      select v.patient_id from vinculations v
      join caretakers c on c.id = v.caretaker_id
      where c.uid = auth.uid()
    )
  );

drop policy if exists event_completions_insert on event_completions;
create policy event_completions_insert on event_completions
  for insert to authenticated with check (
    patient_id in (select id from patients where uid = auth.uid())
    or patient_id in (
      select v.patient_id from vinculations v
      join caretakers c on c.id = v.caretaker_id
      where c.uid = auth.uid()
    )
  );

drop policy if exists event_completions_update on event_completions;
create policy event_completions_update on event_completions
  for update to authenticated using (
    patient_id in (select id from patients where uid = auth.uid())
    or patient_id in (
      select v.patient_id from vinculations v
      join caretakers c on c.id = v.caretaker_id
      where c.uid = auth.uid()
    )
  ) with check (
    patient_id in (select id from patients where uid = auth.uid())
    or patient_id in (
      select v.patient_id from vinculations v
      join caretakers c on c.id = v.caretaker_id
      where c.uid = auth.uid()
    )
  );

drop policy if exists event_completions_delete on event_completions;
create policy event_completions_delete on event_completions
  for delete to authenticated using (
    patient_id in (select id from patients where uid = auth.uid())
    or patient_id in (
      select v.patient_id from vinculations v
      join caretakers c on c.id = v.caretaker_id
      where c.uid = auth.uid()
    )
  );

-- ---------- Realtime (por si no estaban en la publicación) ----------
-- Necesario para que el cambio se propague al cuidador en vivo. Ignora el error
-- si ya están añadidas.
do $$
begin
  begin
    alter publication supabase_realtime add table public.events;
  exception when others then null;
  end;
  begin
    alter publication supabase_realtime add table public.event_completions;
  exception when others then null;
  end;
end $$;

alter table events replica identity full;
alter table event_completions replica identity full;

-- ============================================================================
-- (Opcional) Verificar:
--   select tablename, policyname, cmd from pg_policies
--   where tablename in ('events','event_completions') order by tablename, cmd;
-- ============================================================================
