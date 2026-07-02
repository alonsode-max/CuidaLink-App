-- Teléfono de contacto/emergencia del paciente ("Llamar a casa").
-- Ejecutar en Supabase → SQL Editor. Idempotente.

alter table patients
    add column if not exists emergency_phone text;
