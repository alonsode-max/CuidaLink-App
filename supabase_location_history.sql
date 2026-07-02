-- Historial de ubicaciones del paciente.
-- El paciente inserta un punto periódicamente; el cuidador vinculado los lee.
-- Idempotente: se puede ejecutar varias veces sin error.

create table if not exists location_history (
    id          bigint generated always as identity primary key,
    patient_id  bigint not null references patients (id) on delete cascade,
    lat         double precision not null,
    lng         double precision not null,
    created_at  timestamptz not null default now()
);

-- Índice para leer rápido los últimos puntos de un paciente.
create index if not exists location_history_patient_created_idx
    on location_history (patient_id, created_at desc);

alter table location_history enable row level security;

-- El PACIENTE puede insertar puntos en su propia fila.
drop policy if exists location_history_insert_own on location_history;
create policy location_history_insert_own on location_history
    for insert
    with check (
        exists (
            select 1 from patients p
            where p.id = location_history.patient_id
              and p.uid = auth.uid()
        )
    );

-- El PACIENTE (dueño) o su CUIDADOR vinculado pueden leer el historial.
drop policy if exists location_history_select_linked on location_history;
create policy location_history_select_linked on location_history
    for select
    using (
        exists (
            select 1 from patients p
            where p.id = location_history.patient_id
              and p.uid = auth.uid()
        )
        or exists (
            select 1
            from vinculations v
            join caretakers c on c.id = v.caretaker_id
            where v.patient_id = location_history.patient_id
              and c.uid = auth.uid()
        )
    );
