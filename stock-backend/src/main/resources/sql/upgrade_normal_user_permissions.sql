-- Reset ROLE_NORMAL_USER to the latest menu/data permission policy.
-- Menus are visible, data permissions are readable, and write permission is
-- granted only to the business modules below. "Own data only" is enforced in
-- service code by current user ownership:
-- - stock orders/items: requester_id or operator_id equals current user
-- - request forms/items: user_id equals current user
-- - customers: owner equals current user

DELETE rp
FROM t_role_permission rp
JOIN t_role r ON r.id = rp.role_id
WHERE r.code = 'ROLE_NORMAL_USER';

INSERT INTO t_role_permission (
    role_id,
    permission_id,
    deleted,
    created_by,
    updated_by,
    create_time,
    update_time
)
SELECT
    r.id,
    p.id,
    0,
    1,
    1,
    NOW(),
    NOW()
FROM t_role r
JOIN t_permission p
WHERE r.code = 'ROLE_NORMAL_USER'
  AND r.deleted = 0
  AND p.deleted = 0
  AND p.status = 1
  AND (
      p.type <> 2
      OR RIGHT(p.code, 5) = '_READ'
      OR (
          RIGHT(p.code, 6) = '_WRITE'
          AND (
              p.path = '/api/stockOrder' OR p.path LIKE '/api/stockOrder/%'
              OR p.path = '/api/stockOrderItem' OR p.path LIKE '/api/stockOrderItem/%'
              OR p.path = '/api/requestForm' OR p.path LIKE '/api/requestForm/%'
              OR p.path = '/api/requestItem' OR p.path LIKE '/api/requestItem/%'
              OR p.path = '/api/customer' OR p.path LIKE '/api/customer/%'
              OR p.path = '/api/customerLevel' OR p.path LIKE '/api/customerLevel/%'
          )
      )
  );
