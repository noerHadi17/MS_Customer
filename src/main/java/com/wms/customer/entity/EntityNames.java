package com.wms.customer.entity;

public final class EntityNames {
    private EntityNames() {}

    public static final String MST_CUSTOMER = "mst_customer";

    public static final class MstCustomer {
        private MstCustomer() {}
        public static final String CUSTOMER_ID = "customer_id";
        public static final String NAME = "name";
        public static final String EMAIL = "email";
        public static final String PASSWORD_HASH = "password_hash";
        public static final String NIK = "nik";
        public static final String ADDRESS = "address";
        public static final String ID_RISK_PROFILE = "id_risk_profile";
        public static final String DOB = "dob";
        public static final String POB = "pob";
    }
}

