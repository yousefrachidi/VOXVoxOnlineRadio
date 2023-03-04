package nemosofts.vox.radio.item;

public class ItemPlan {

    private final String planId;
    private final String planName;
    private final String planDuration;
    private final String planPrice;
    private final String planCurrencyCode;
    private final String subscription_id;
    private final String base_key;

    public ItemPlan(String planId, String planName, String planDuration, String planPrice, String planCurrencyCode, String subscription_id, String base_key) {
        this.planId = planId;
        this.planName = planName;
        this.planDuration = planDuration;
        this.planPrice = planPrice;
        this.planCurrencyCode = planCurrencyCode;
        this.subscription_id = subscription_id;
        this.base_key = base_key;
    }

    public String getPlanId() {
        return planId;
    }

    public String getPlanName() {
        return planName;
    }

    public String getPlanDuration() {
        return planDuration;
    }

    public String getPlanPrice() {
        return planPrice;
    }

    public String getPlanCurrencyCode() {
        return planCurrencyCode;
    }

    public String getSubscription_id() {
        return subscription_id;
    }

    public String getBase_key() {
        return base_key;
    }
}
