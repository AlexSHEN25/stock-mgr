-- Normalize legacy negative request quantities.
-- Rule: request quantity and out quantity must be non-negative.

UPDATE t_request_item
SET request_qty = ABS(request_qty)
WHERE deleted = 0
  AND request_qty < 0;

UPDATE t_request_item
SET out_qty = ABS(out_qty)
WHERE deleted = 0
  AND out_qty < 0;
