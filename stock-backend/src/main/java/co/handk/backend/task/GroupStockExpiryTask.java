package co.handk.backend.task;

import co.handk.backend.service.StockBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupStockExpiryTask {
    private final StockBatchService stockBatchService;

    @Scheduled(cron = "${stock.group-expiry-cron:0 5 * * * *}")
    public void reclaimExpiredStock() {
        int quantity = stockBatchService.reclaimExpiredGroupStock();
        if (quantity > 0) {
            log.info("Reclaimed expired group stock: quantity={}", quantity);
        }
    }
}
