package co.handk.backend.task;

import co.handk.backend.util.StringRedisUtil;
import co.handk.backend.service.StockBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupStockExpiryTask {
    private static final String LOCK_KEY = "stock:group-expiry:lock";

    private final StockBatchService stockBatchService;
    private final StringRedisUtil stringRedisUtil;

    @Scheduled(cron = "${stock.group-expiry-cron:0 5 * * * *}")
    public void reclaimExpiredStock() {
        Boolean locked = stringRedisUtil.setIfAbsent(LOCK_KEY, "1", 10, TimeUnit.MINUTES);
        if (Boolean.FALSE.equals(locked)) {
            log.info("Skip expired group stock reclaim because another worker is running");
            return;
        }
        try {
            int quantity = stockBatchService.reclaimExpiredGroupStock();
            if (quantity > 0) {
                log.info("Reclaimed expired group stock: quantity={}", quantity);
            }
        } finally {
            stringRedisUtil.delete(LOCK_KEY);
        }
    }
}
