-- ============================================================
-- 006: Pin search_path on every SECURITY DEFINER function in public
--
-- Sign-up failed with "Database error saving new user": the
-- on_auth_user_created trigger runs as supabase_auth_admin, whose
-- search_path does not include public, so handle_new_user() could not
-- find the profiles table. Unpinned search_path on SECURITY DEFINER
-- functions is also a privilege-escalation risk, so pin all of them.
-- ============================================================

DO $$
DECLARE
  fn regprocedure;
BEGIN
  FOR fn IN
    SELECT p.oid::regprocedure
    FROM pg_proc p
    JOIN pg_namespace n ON n.oid = p.pronamespace
    WHERE n.nspname = 'public'
      AND p.prosecdef
  LOOP
    EXECUTE format('ALTER FUNCTION %s SET search_path = public', fn);
  END LOOP;
END;
$$;
