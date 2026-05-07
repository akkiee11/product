package in.rinmukt.domain;

public enum DebtType {
    CREDIT_CARD,        // Revolving CC at 36-42% APR
    CC_EMI,             // CC outstanding converted to EMI loan
    PERSONAL_LOAN,      // Bank/NBFC personal loan
    BNPL,               // Lazypay, Simpl, ZestMoney
    HOME_LOAN,          // Secured, low rate
    AUTO_LOAN,          // Secured, medium rate
    EDUCATION_LOAN,
    GOLD_LOAN,
    INFORMAL,           // Family/friends
    OTHER;

    public boolean isUnsecured() {
        return this == CREDIT_CARD || this == CC_EMI || this == PERSONAL_LOAN || this == BNPL;
    }

    public boolean isSettleable() {
        return this == CREDIT_CARD || this == PERSONAL_LOAN;
    }
}
