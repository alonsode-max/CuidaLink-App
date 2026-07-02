-- Bucket de fotos de perfil (paciente y cuidador).
-- Ejecutar en Supabase → SQL Editor. Idempotente.

-- 1) Crea el bucket "avatars" público (para poder mostrar la foto por URL).
insert into storage.buckets (id, name, public)
values ('avatars', 'avatars', true)
on conflict (id) do update set public = true;

-- 2) Políticas sobre storage.objects para el bucket "avatars".
--    Cualquier usuario autenticado puede subir/reemplazar su foto; lectura pública.

drop policy if exists avatars_read_public on storage.objects;
create policy avatars_read_public on storage.objects
    for select
    using (bucket_id = 'avatars');

drop policy if exists avatars_insert_auth on storage.objects;
create policy avatars_insert_auth on storage.objects
    for insert to authenticated
    with check (bucket_id = 'avatars');

drop policy if exists avatars_update_auth on storage.objects;
create policy avatars_update_auth on storage.objects
    for update to authenticated
    using (bucket_id = 'avatars')
    with check (bucket_id = 'avatars');

drop policy if exists avatars_delete_auth on storage.objects;
create policy avatars_delete_auth on storage.objects
    for delete to authenticated
    using (bucket_id = 'avatars');
