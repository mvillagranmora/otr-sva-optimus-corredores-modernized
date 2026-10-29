/**
 * Records of the "envío a SAP" chain (WP-B, CONT-B-*): SAP Business One Service Layer payloads, the
 * PreComprobante*EnvioContabilidad staging rows and the OACT chart of accounts.
 *
 * <p>Component names deliberately keep the external field names (SAP JSON fields, HANA column labels and the
 * {@code :Name} parameters of the legacy SQL): they are an external contract serialised verbatim, so SAP wire JSON,
 * JavaCall output and the staging table columns all use exactly these spellings.
 */
package cl.bcs.contabilidad.domain.model.sap;
