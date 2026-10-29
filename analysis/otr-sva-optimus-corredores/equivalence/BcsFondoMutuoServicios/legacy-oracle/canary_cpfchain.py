"""Canary of the goldens of the CHAIN (CPF over the REAL FFMM), F3 W1 front 9, part B. Same technique as canary_ffmm.py / canary_cpf.py (it reuses their machinery): each mutation is a ONE-LINE change of a COPY
of a legacy source (CPF or FFMM: LEGACY_OVERLAY) or of one of the SQL (LEGACY_OVERLAY_RES); the oracle of the chain (record_cpfchain.sh) is re-run on it with ORACLE_OUT (the goldens are NOT rewritten) and the
dumps are compared with cases-cpfchain/*/expected.json. Only the mutations that must be visible THROUGH the chain are listed (what CPF reads of the custody answer, and what FFMM does with what CPF asks); the
full list of FFMM mutations is run against its own goldens by canary_ffmm.py.

  py -3 -I legacy-oracle/canary_cpfchain.py [java|sql] [text of the mutation name]
"""
import pathlib
import sys

sys.path.insert(0, str(pathlib.Path(__file__).resolve().parent))   # python -I does not put the script directory on the path
import canary_ffmm as C  # noqa: E402

CPF = "ConsultarCustodiaPortafolioFondoMutuo.java"
FFMM = C.CLASE
SQL = C.SQL

MUTACIONES = [
    # ---- what CPF does with the custody answer (FM-CPFF-22)
    ("java", CPF, "salida.IdBoveda = \"COR\";", "salida.IdBoveda = \"DCV\";", "the vault of a saldo WITHOUT custody (FM-CPFF-22.1: with a request in transit the real FFMM answers a row, so the saldo has custody)", ""),
    ("java", CPF, "instrumento.Custodia = custodia;", "if(instrumento.Custodia == null) instrumento.Custodia = custodia;", "the FIRST custody of a CPF key wins instead of the LAST (FM-CPFF-22.4, D-F3C-13)", ""),
    ("java", CPF, "salida.PPPCustodia = saldoPortafolioRegimenInstrumento.Custodia.PPPCustodia != null ? saldoPortafolioRegimenInstrumento.Custodia.PPPCustodia : BigDecimal.ZERO;",
     "salida.PPPCustodia = saldoPortafolioRegimenInstrumento.Custodia.PPPCustodia;", "a null PPPCustodia stays null instead of 0 (FM-CPFF-22.2)", ""),
    ("java", CPF, "String key = solicitud.IdCliente + \"_\" + solicitud.IdPortafolio + \"_\" + solicitud.IdInstrumento + \"_\" + solicitud.IdRegimenFondoMutuo;",
     "String key = solicitud.IdCliente + \"_\" + solicitud.IdPortafolio + \"_\" + solicitud.IdInstrumento;", "the regimen is not part of the saldo key (FM-CPF-07)", ""),
    ("java", CPF, "Boolean esFechaConsultaValida = _c.FechaDefecto.equals(fechaConsulta) || fechaConsulta == null;", "Boolean esFechaConsultaValida = _c.FechaDefecto.equals(fechaConsulta);",
     "a null date is not the default date: it reaches FFMM as null (FM-CPFF-22.5)", ""),
    ("java", CPF, "String fechaConsulta = _c.FechaDefecto;\n", "String fechaConsulta = _c.FechaDefecto;\n\t\tfechaSistema = _c.FechaDefecto;\n", "the default date reaches FFMM as '0001-01-01' instead of the system date (FM-CPFF-22.5, D-F3C-04)", ""),
    ("java", CPF, "salida.CantidadGarantia = saldoPortafolioRegimenInstrumento.Custodia.CantidadGarantia", "salida.CantidadGarantia = BigDecimal.ZERO", "CPF drops the guarantee quantity of the custody answer (FM-CPFF-22.2)", ""),
    ("java", CPF, "salida.IdBoveda = saldoPortafolioRegimenInstrumento.Custodia.IdBoveda;", "salida.IdBoveda = \"COR\";", "CPF takes the vault of the custody answer (FM-CPFF-22.3)", ""),
    ("java", CPF, "salida.Relacion = saldoPortafolioRegimenInstrumento.Custodia.Relacion;", "salida.Relacion = \"CPRO\";", "CPF takes the relation of the custody answer (FM-CPFF-22.3)", ""),
    # ---- what FFMM answers (every one of these changes the CPF output through the chain)
    ("java", FFMM, "salida.MontoPrecioPromedio = BigDecimal.ZERO;\n\t\t\t\t};", "salida.MontoPrecioPromedio = salida.MontoPrecioPromedio;\n\t\t\t\t};", "the price amount survives a guarantee: PPPCustodia of CPF would not be 0 (D-F3C-01 'fixed')", ""),
    ("java", FFMM, "params.put(\"FechaConsulta\", entrada.p_FechaConsulta);", "params.put(\"FechaConsulta\", \"0001-01-01\");", "FFMM always receives the default date (FM-CPFF-22.5)", ""),
    ("java", FFMM, "params.put(\"TipoFechaConsulta\", entrada.p_TipoFechaConsulta);", "params.put(\"TipoFechaConsulta\", \"ADQ\");", "FFMM is asked ADQ instead of LIQ (D-F3C-05)", ""),
    ("java", FFMM, "boveda = cus.IdBoveda;", "boveda = \"\";", "no effect through CPF (it asks N): an equivalent mutant of the chain", "CPF always asks p_AgrupaPorBoveda N: this branch is never taken through the chain"),
    ("java", FFMM, "HashMap<String,Instrumento> hInstrumento = new HashMap<String,Instrumento>();", "HashMap<String,Instrumento> hInstrumento = new java.util.LinkedHashMap<String,Instrumento>();",
     "insertion order in FFMM: the winner of a tie in CPF (the LAST custody of a CPF key) changes (FM-CPFF-22.4, D-F3C-13)", ""),
    ("java", FFMM, "custodia.Cantidad = custodia.Cantidad.add(cus.Cantidad, MathContext.DECIMAL128);", "custodia.Cantidad = custodia.Cantidad.add(cus.Cantidad, MathContext.DECIMAL32);", "quantity summed with 7 digits (FM-CPFF-13)", "",),
    ("java", FFMM, "if(\"0001-01-01\".equals(entrada.p_FechaConsulta) || dFechaEntrada.compareTo(dFechaSistema) != -1) {", "if(\"0001-01-01\".equals(entrada.p_FechaConsulta) || dFechaEntrada.compareTo(dFechaSistema) == 1) {",
     "a date equal to the system date is historical (guarantee) (FM-CPFF-07)", ""),
    ("java", FFMM, "salida.PPPCustodia = salida.MontoPrecioPromedio.divide(salida.CantidadPrecioPromedio, MathContext.DECIMAL128);", "salida.PPPCustodia = salida.MontoPrecioPromedio.divide(salida.CantidadPrecioPromedio, MathContext.DECIMAL64);",
     "PPP with 16 digits (FM-CPFF-15)", ""),
    ("java", FFMM, "salida.INGPrecioPromedioCompra = custodia.ValorCompra.divide(custodia.Cantidad, MathContext.DECIMAL128);", "salida.INGPrecioPromedioCompra = custodia.MontoCompra.divide(custodia.Cantidad, MathContext.DECIMAL128);",
     "the average purchase price divides the rate amount (FM-CPFF-15)", ""),
    ("java", FFMM, "if(_c.SI.equals(entrada.p_AgrupaPorRelacion)) {", "if(true) {", "FFMM groups by relation although CPF asks N (FM-CPFF-22.3)", "", 1),
    ("sql", SQL % C.CUS, ",'' AS \"IdBoveda\"", ",'COR' AS \"IdBoveda\"", "the request branch of the custody carries vault COR (FM-CPFF-22.1)",
     "not observable through CPF: it asks p_AgrupaPorBoveda N, so the vault of every group is '' whatever the SQL column holds (the FFMM canary pins it with S)"),
    ("sql", SQL % C.CUS, "Solicitudes.\"Estado\" IN ('ING', 'PEN')", "Solicitudes.\"Estado\" IN ('ING')", "PEN requests are not in the custody answer (FM-CPFF-22.1)", ""),
    ("sql", SQL % C.CUS, "AND Solicitudes.\"FechaAceptacion\" <= :FechaConsulta", "AND Solicitudes.\"FechaAceptacion\" < :FechaConsulta", "a request accepted on the query date is not in the custody answer (FM-CPFF-22.1)", ""),
    ("sql", SQL % C.CUS, "AND Cartera.\"IdCliente\" = Solicitudes.\"IdCliente\"", "AND 1 = 1", "the NOT IN looks at every client: a request disappears when ANY client holds the series (FM-CPFF-22.1)", ""),
    ("sql", SQL % C.CUSH, "AND :IdEmisor = :IdEmisor\n", "\n", "the historical request branch has no issuer tautology (FM-CPFF-22.1, D-F3C-06)", ""),
    ("sql", SQL % C.CUSH, "WHERE RANK = 1", "WHERE RANK >= 1", "every version of a folio reaches CPF (FM-CPFF-09)", ""),
    ("sql", SQL % C.CUS, "CAR.\"IdEstado\" = 'VIG'", "CAR.\"IdEstado\" <> 'ANU'", "current custody takes every state but ANU (FM-CPFF-20)", ""),
    ("sql", SQL % C.CUS, "AND (:IdEmisor IS NULL OR CAR.\"IdEmisor\" = :IdEmisor)", "", "current custody ignores the issuer filter CPF forwards (FM-CPFF-06)", ""),
    ("sql", SQL % C.CUS, "Serie.\"IdFamiliaFondo\" = CASE WHEN IFNULL(:IdFamiliaFondo,'') <> '' THEN IFNULL(:IdFamiliaFondo,'') ELSE Serie.\"IdFamiliaFondo\" END", "1 = 1", "the family filter CPF forwards is ignored (FM-CPFF-06)", ""),
    ("sql", SQL % C.GAR, "GAR.\"IdEstado\" <> 'ANU'", "GAR.\"IdEstado\" = 'VIG'", "a guarantee in any state but VIG is dropped (FM-CPFF-10)", ""),
    ("sql", SQL % C.GAR, ":FechaConsulta < CASE", ":FechaConsulta <= CASE", "the guarantee egress date is inclusive (FM-CPFF-10)", ""),
    ("sql", SQL % C.PER, "SELECT distinct PER.\"IdPersonaDestino\"", "SELECT PER.\"IdPersonaDestino\"", "the related persons are not DISTINCT (FM-CPFF-11)", "no chain case has a repeated active relation of the company: not observable through CPF"),
]


def main():
    C.ejecutar(MUTACIONES, "record_cpfchain.sh", C.BASE / "cases-cpfchain", "FM-CHAIN", sys.argv)


if __name__ == "__main__":
    main()
