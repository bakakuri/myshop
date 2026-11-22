-- ============================================================
-- 005: Harden message policies
--
-- 1. A customer could insert a message with sender_type = 'staff' and
--    appear in the chat as the store. Non-staff may only send as 'customer'.
-- 2. Any sender could UPDATE their own messages (content, sender_type,
--    is_internal_note). Customers never need direct UPDATE: read receipts go
--    through mark_conversation_read() (SECURITY DEFINER). Staff only.
-- ============================================================

DROP POLICY IF EXISTS "Participants can send messages" ON messages;

CREATE POLICY "Participants can send messages"
  ON messages FOR INSERT
  WITH CHECK (
    auth.uid() = sender_id
    AND is_conversation_participant(conversation_id)
    AND (
      (sender_type = 'customer' AND is_internal_note = FALSE)
      OR can_manage_conversations()
    )
  );

DROP POLICY IF EXISTS "Staff can update message read status" ON messages;

CREATE POLICY "Staff can update messages"
  ON messages FOR UPDATE
  USING (can_manage_conversations())
  WITH CHECK (can_manage_conversations());

-- mark_conversation_read() runs as definer; keep search_path pinned
ALTER FUNCTION mark_conversation_read(UUID) SET search_path = public;
