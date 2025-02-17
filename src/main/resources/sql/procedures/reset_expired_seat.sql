CREATE EVENT reset_expired_seat
    ON SCHEDULE EVERY 1 MINUTE
    DO
    UPDATE show_seat
    SET status          = 'AVAILABLE',
        expiration_time = NULL
    WHERE status = 'payment_pending'
      AND expiration_time < CURRENT_TIMESTAMP;