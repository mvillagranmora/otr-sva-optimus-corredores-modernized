package cl.bcs.contabilidad.domain.model.sap;

/**
 * SAP B1 {@code BusinessPartners} payload (legacy pojo/BusinessPartner.java). Address, City, County and Currency
 * are the fixed legacy placeholders, including the legacy spelling "Adress".
 */
public record BusinessPartner(String CardCode, String CardName, String CardType, String FederalTaxID,
                              String Address, String City, String County, String GroupCode, String Currency) {

    public static final String CONTRAPARTE = "100";
    public static final String PROVEEDOR = "101";
    public static final String CLIENTE = "100";
    public static final String TIPO_CLIENTE = "cCustomer";
    public static final String TIPO_PROVEEDOR = "cSupplier";

    /** Legacy constructor {@code BusinessPartner(CardCode, Rut, CardName, CardType, GroupCode)}. */
    public static BusinessPartner of(String cardCode, String rut, String cardName, String cardType, String groupCode) {
        return new BusinessPartner(cardCode, cardName, cardType, rut, "Adress", "City", "County", groupCode, "##");
    }
}
