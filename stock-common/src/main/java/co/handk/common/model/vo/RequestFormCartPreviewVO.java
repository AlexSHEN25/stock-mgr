package co.handk.common.model.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class RequestFormCartPreviewVO {
    private String templateCode;
    private LocalDate invoiceDate;
    private CompanyInfo company;
    private CustomerInfo customer;
    private ShippingInfo shipping;
    private BankInfo bank;
    private List<String> columns = new ArrayList<>();
    private List<Item> items = new ArrayList<>();
    private Integer totalQty;
    private BigDecimal totalAmt;
    private String currency;

    @Data
    public static class CompanyInfo {
        private String name;
        private String tel;
        private String email;
        private String address;
    }

    @Data
    public static class CustomerInfo {
        private Long id;
        private String code;
        private String name;
        private String englishName;
        private String contactPerson;
        private String phone;
        private String email;
        private String country;
        private String city;
        private String address;
    }

    @Data
    public static class ShippingInfo {
        private String term;
        private String countryOfOrigin;
        private String transportedFrom;
    }

    @Data
    public static class BankInfo {
        private String title;
        private String bankName;
        private String bankAddress;
        private String swiftCode;
        private String accountNo;
        private String accountType;
        private String branchNo;
        private String branchName;
        private String beneficiaryName;
        private String beneficiaryAddress;
        private String notice;
    }

    @Data
    public static class Item {
        private Integer no;
        private String brandName;
        private String itemName;
        private String goodsName;
        private String skuCode;
        private String seriesName;
        private String makerName;
        private String categoryName;
        private String stockTypeName;
        private LocalDate bizDate;
        private Integer qty;
        private BigDecimal unitPrice;
        private BigDecimal price;
        private String currency;
        private String remark;
        private String hsCode;
        private Long stockRecordId;
        private List<Long> stockRecordIds;
        private Long stockOrderItemId;
        private List<Long> stockOrderItemIds;
    }
}
