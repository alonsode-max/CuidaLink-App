-- Varias geovallas (zonas seguras) por paciente, guardadas como array JSON en la
-- propia fila del paciente. Así el cuidador las recibe por el realtime que ya existe.
-- Ejecutar en Supabase → SQL Editor. Idempotente.
--
-- Formato del array: [{"lat": 40.41, "lng": -3.70, "radius": 500, "name": "Casa"}, ...]

alter table patients
    add column if not exists geofences jsonb not null default '[]'::jsonb;
