package nemosofts.vox.radio.activity;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.nemosofts.lk.ProCompatActivityNormal;



import java.util.ArrayList;
import java.util.List;

import nemosofts.vox.radio.BuildConfig;
import nemosofts.vox.radio.R;
import nemosofts.vox.radio.callback.Callback;
import nemosofts.vox.radio.ifSupported.IsRTL;
import nemosofts.vox.radio.ifSupported.IsScreenshot;
import nemosofts.vox.radio.utils.ApplicationUtil;
import nemosofts.vox.radio.utils.SharedPref;
import nemosofts.vox.radio.utils.purchases.BillingConnector;
import nemosofts.vox.radio.utils.purchases.BillingEventListener;
import nemosofts.vox.radio.utils.purchases.Transaction;
import nemosofts.vox.radio.utils.purchases.enums.ProductType;
import nemosofts.vox.radio.utils.purchases.models.BillingResponse;
import nemosofts.vox.radio.utils.purchases.models.ProductInfo;
import nemosofts.vox.radio.utils.purchases.models.PurchaseInfo;


public class BillingConnectorActivity extends ProCompatActivityNormal {

    private String subscription_id, base_key;

    private BillingConnector billingConnector;
    //this is the variable in which we'll store the status of the purchase
    //once we'll have the data stored, we can retrieve it in any activity or fragment,
    //to update the code and the UI accordingly to the user purchase
    private boolean userPrefersAdFree = false;

    private String planId;
    private String planName;
    private String planPrice;
    private String planDuration;
    private String planCurrencyCode;
    private TextView btn_proceed;
    private SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        IsRTL.ifSupported(this);
        IsScreenshot.ifSupported(this);

        sharedPref = new SharedPref(this);

        Intent intent = getIntent();
        planId = intent.getStringExtra("planId");
        planName = intent.getStringExtra("planName");
        planPrice = intent.getStringExtra("planPrice");
        planDuration = intent.getStringExtra("planDuration");
        planCurrencyCode= intent.getStringExtra("planCurrencyCode");

        subscription_id = intent.getStringExtra("subscription_id");
        base_key = intent.getStringExtra("base_key");

        initViews();
        initializeBillingClient();

        btn_proceed.setOnClickListener(v -> {
            //purchase a subscription without an offer (only a base plan)
            billingConnector.subscribe(BillingConnectorActivity.this, subscription_id);
        });

        findViewById(R.id.tv_terms).setOnClickListener(v -> {
            Intent intent1 = new Intent(BillingConnectorActivity.this, WebActivity.class);
            intent1.putExtra("web_url", BuildConfig.BASE_URL+"terms.php");
            intent1.putExtra("page_title", getResources().getString(R.string.terms_and_conditions));
            ActivityCompat.startActivity(BillingConnectorActivity.this, intent1, null);
        });

        findViewById(R.id.changePlan).setOnClickListener(view -> finish());
    }

    @SuppressLint("SetTextI18n")
    private void initViews() {
        TextView textPlanName = findViewById(R.id.textPackName);
        TextView textPlanPrice = findViewById(R.id.textPrice);
        TextView textPlanCurrency = findViewById(R.id.textCurrency);
        TextView textPlanDuration = findViewById(R.id.textDay);
        TextView textChoosePlanName = findViewById(R.id.choosePlanName);
        TextView textEmail = findViewById(R.id.planEmail);

        btn_proceed = findViewById(R.id.tv_btn_proceed);
        btn_proceed.setText("Pay for "+ planPrice+" "+planCurrencyCode);

        textPlanName.setText(planName);
        textPlanPrice.setText(planPrice);
        textPlanDuration.setText(getString(R.string.plan_day_for, planDuration)+" Days");
        textChoosePlanName.setText(planName);
        textEmail.setText(sharedPref.getEmail());
        textPlanCurrency.setText(planCurrencyCode);
    }

    private void initializeBillingClient() {
        //create a list with subscription ids
        List<String> subscriptionIds = new ArrayList<>();
        subscriptionIds.add(subscription_id);

        billingConnector = new BillingConnector(this, base_key) //"license_key" - public developer key from Play Console
                .setSubscriptionIds(subscriptionIds) //to set subscription ids - call only for subscription products
                .autoAcknowledge() //legacy option - better call this. Alternatively purchases can be acknowledge via public method "acknowledgePurchase(PurchaseInfo purchaseInfo)"
                .connect(); //to connect billing client with Play Console

        billingConnector.setBillingEventListener(new BillingEventListener() {
            @Override
            public void onProductsFetched(@NonNull List<ProductInfo> productDetails) {
                // this method is empty
            }

            //this IS the listener in which we can restore previous purchases
            @Override
            public void onPurchasedProductsFetched(@NonNull ProductType productType, @NonNull List<PurchaseInfo> purchases) {
                String purchasedProduct;
                boolean isAcknowledged;

                for (PurchaseInfo purchaseInfo : purchases) {
                    purchasedProduct = purchaseInfo.getProduct();
                    isAcknowledged = purchaseInfo.isAcknowledged();

                    if (!userPrefersAdFree) {
                        if (purchasedProduct.equalsIgnoreCase(subscription_id)) {
                            if (isAcknowledged) {
                                btn_proceed.setVisibility(View.INVISIBLE);
                                userPrefersAdFree = true;
                                new Transaction(BillingConnectorActivity.this).purchasedItem(planId, planName, planPrice, planDuration, planCurrencyCode);
                                Toast.makeText(BillingConnectorActivity.this, "The previous purchase was successfully restored.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            }

            //this IS NOT the listener in which we'll give user entitlement for purchases (see ReadMe.md why)
            @Override
            public void onProductsPurchased(@NonNull List<PurchaseInfo> purchases) {
                // this method is empty
            }

            //this IS the listener in which we'll give user entitlement for purchases (the ReadMe.md explains why)
            @Override
            public void onPurchaseAcknowledged(@NonNull PurchaseInfo purchase) {
                /*
                 * Grant user entitlement for NON-CONSUMABLE products and SUBSCRIPTIONS here
                 *
                 * Even though onProductsPurchased is triggered when a purchase is successfully made
                 * there might be a problem along the way with the payment and the purchase won't be acknowledged
                 *
                 * Google will refund users purchases that aren't acknowledged in 3 days
                 *
                 * To ensure that all valid purchases are acknowledged the library will automatically
                 * check and acknowledge all unacknowledged products at the startup
                 * */
                String acknowledgedProduct = purchase.getProduct();
                if (acknowledgedProduct.equalsIgnoreCase(subscription_id)) {
                    btn_proceed.setVisibility(View.INVISIBLE);
                    userPrefersAdFree = true;
                    new Transaction(BillingConnectorActivity.this).purchasedItem(planId, planName, planPrice, planDuration, planCurrencyCode);
                    Toast.makeText(BillingConnectorActivity.this, "The purchase was successfully made.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onPurchaseConsumed(@NonNull PurchaseInfo purchase) {
                /*
                 * Grant user entitlement for CONSUMABLE products here
                 *
                 * Even though onProductsPurchased is triggered when a purchase is successfully made
                 * there might be a problem along the way with the payment and the user will be able consume the product
                 * without actually paying
                 * */
            }

            @Override
            public void onBillingError(@NonNull BillingConnector billingConnector, @NonNull BillingResponse response) {
                String error_msg = "";
                switch (response.getErrorType()) {
                    case CLIENT_NOT_READY:
                        error_msg = "Client is not ready yet";
                        break;
                    case CLIENT_DISCONNECTED:
                        error_msg = "Client has disconnected";
                        break;
                    case PRODUCT_NOT_EXIST:
                        error_msg = "Product does not exist";
                        break;
                    case CONSUME_ERROR:
                        error_msg = "Error during consumption";
                        break;
                    case CONSUME_WARNING:
                        error_msg = "Warning during consumption";
                        break;
                    case ACKNOWLEDGE_ERROR:
                        error_msg = "Error during acknowledgment";
                        break;
                    case ACKNOWLEDGE_WARNING:
                        //this response will be triggered when the purchase is still PENDING
                        error_msg = "The transaction is still pending. Please come back later to receive the purchase!";
                        break;
                    case FETCH_PURCHASED_PRODUCTS_ERROR:
                        error_msg = "Error occurred while querying purchased products";
                        break;
                    case BILLING_ERROR:
                        error_msg = "error occurred during initialization / querying product details";
                        break;
                    case USER_CANCELED:
                        error_msg = "user pressed back or canceled a dialog";
                        break;
                    case SERVICE_UNAVAILABLE:
                        error_msg = "Check your internet connection!";
                        break;
                    case BILLING_UNAVAILABLE:
                        error_msg = "Billing is unavailable at the moment.";
                        break;
                    case ITEM_UNAVAILABLE:
                        error_msg = "requested product is not available for purchase";
                        break;
                    case DEVELOPER_ERROR:
                        error_msg = "invalid arguments provided to the API";
                        break;
                    case ERROR:
                        error_msg = "Something happened, the transaction was canceled!";
                        break;
                    case ITEM_ALREADY_OWNED:
                        error_msg = "Failure to purchase since item is already owned";
                        break;
                    case ITEM_NOT_OWNED:
                        error_msg = "failure to consume since item is not owned";
                        break;
                }
                Toast.makeText(BillingConnectorActivity.this, error_msg, Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected int setLayoutResourceId() {
        return R.layout.activity_billing_connector;
    }

    @Override
    protected int setApplicationThemes() {
        return ApplicationUtil.isTheme();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (billingConnector != null) {
            billingConnector.release();
        }
    }

}