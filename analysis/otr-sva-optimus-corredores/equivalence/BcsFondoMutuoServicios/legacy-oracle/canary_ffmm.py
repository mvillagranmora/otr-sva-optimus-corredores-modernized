"""Canary of the goldens of FM-CPFF (ConsultarCustodiaPortafolioFFMM), F3 W1 front 9, part A.

The new code does not exist when the goldens are recorded, so it cannot be mutated: as CPF (canary_cpf.py), FSA / SFM / DET and F2 plan section 7 ("mutation of goldens"), each mutation is a
ONE-LINE change of a COPY of the legacy source (LEGACY_OVERLAY, a temporary directory: nothing under legacy/ is touched) or of one of its five SQL (LEGACY_OVERLAY_RES, a temporary directory with
cl/bcs/fondomutuo/sql/<file>, put BEFORE the legacy tree on the class path). The oracle is re-run on it with ORACLE_OUT (the goldens are NOT rewritten), one repetition, and the dumps are
compared with the recorded goldens. A mutation that changes no dump means the case set does not pin that behaviour (an EQUIVALENT mutant, or a hole to report).

  py -3 -I legacy-oracle/canary_ffmm.py [java|sql] [text of the mutation name]      (needs JAVA_HOME / bash like the record scripts; prints one line per mutation and a total)
  CANARY_RANGE=a:b splits the list over parallel copies; ORACLE_REUSE_CLASSES=<work dir> reuses the compiled classes for the SQL mutations (every parallel copy needs its OWN copy of that directory).

The "expected equivalent" mutants are listed with the reason (the property is not observable at the boundary of the class): they are reported, not counted as holes.
The generic machinery (reemplazar / comparable / correr / ejecutar) is reused by canary_cpfchain.py.
"""
import json
import os
import pathlib
import subprocess
import sys
import tempfile

HERE = pathlib.Path(__file__).resolve().parent
BASE = HERE.parent
WS = BASE.parents[3]
FMP = WS / "legacy" / "otr-sva-optimus-corredores" / "BSCL" / "FondoMutuo" / "JAVA" / "BcsFondoMutuoServicios" / "src" / "cl" / "bcs" / "fondomutuo"
_GIT = "C:/Program Files/Git/usr/bin/bash.exe"
BASH = os.environ.get("BASH_EXE") or (_GIT if os.path.exists(_GIT) else "bash")   # on Windows a bare "bash" may be the WSL launcher, which does not understand C:/ paths
CLASE = "ConsultarCustodiaPortafolioFFMM.java"
SQL = "ConsultarCustodiaPortafolioFFMM_%s.sql"
CUS, CUSH, GAR, GARH, PER = "SelectConsultarCustodia", "SelectConsultarCustodiaHistorico", "SelectConsultarGarantia", "SelectConsultarGarantiaHistorico", "SelectPersonaDestino"
EXTRA = "ConsultarCustodiaPortafolioFFMM"

# (kind, file, old text, new text, what a rewrite could plausibly get wrong, equivalent-mutant reason or ""[, n = which occurrence of the old text, default 1])
MUTACIONES = [
    # ---- the date and the source (FM-CPFF-05 / 07)
    ("java", CLASE, "dFechaEntrada.compareTo(dFechaSistema) != -1", "dFechaEntrada.compareTo(dFechaSistema) == 1", "a date equal to PLA.FechaSistema is historical (guarantee) (FM-CPFF-07)", ""),
    ("java", CLASE, "dFechaEntrada.compareTo(dFechaSistema) != -1", "dFechaEntrada.compareTo(dFechaSistema) == 1", "a date equal to PLA.FechaSistema is historical (custody) (FM-CPFF-07)", "", 2),
    ("java", CLASE, "\"0001-01-01\".equals(entrada.p_FechaConsulta) || dFechaEntrada", "dFechaEntrada", "the default date '0001-01-01' is historical (guarantee) (FM-CPFF-07, D-F3C-04)", ""),
    ("java", CLASE, "\"0001-01-01\".equals(entrada.p_FechaConsulta) || dFechaEntrada", "dFechaEntrada", "the default date '0001-01-01' is historical (custody) (FM-CPFF-07, D-F3C-04)", "", 2),
    ("java", CLASE, "params.put(\"FechaConsulta\", entrada.p_FechaConsulta);", "params.put(\"FechaConsulta\", fechaSistema);", "the default date is replaced by the system date before the SQL (D-F3C-04)", ""),
    ("java", CLASE, "Date dFechaEntrada = Fecha.toDate(entrada.p_FechaConsulta);", "Date dFechaEntrada = Fecha.toDate(entrada.p_FechaConsulta == null ? fechaSistema : entrada.p_FechaConsulta);",
     "a null date falls back to the system date instead of raising (FM-CPFF-05)", ""),
    ("java", CLASE, "params.put(\"TipoFechaConsulta\", entrada.p_TipoFechaConsulta);", "params.put(\"TipoFechaConsulta\", \"LIQ\");", "the date type is always LIQ (FM-CPFF-06)", ""),
    ("java", CLASE, "params.put(\"IdEmisor\", entrada.p_IdEmisor);", "params.put(\"IdEmisor\", null);", "the issuer filter is ignored (FM-CPFF-06)", ""),
    ("java", CLASE, "params.put(\"IdFamiliaFondo\", entrada.p_IdFamiliaFondo);", "params.put(\"IdFamiliaFondo\", \"\");", "the family filter is ignored (FM-CPFF-06)", ""),
    ("java", CLASE, "params.put(\"IdProductoAgrupador\", entrada.p_IdProductoAgrupador);", "params.put(\"IdProductoAgrupador\", null);", "the product filter is ignored (FM-CPFF-06)", ""),
    ("java", CLASE, "params.put(\"IdInstrumento\", entrada.p_IdInstrumento);", "params.put(\"IdInstrumento\", 0L);", "the instrument filter is ignored (FM-CPFF-06)", ""),
    ("java", CLASE, "params.put(\"IdCliente\", entrada.p_IdCliente);", "params.put(\"IdCliente\", 0L);", "the client filter is ignored (FM-CPFF-06)", ""),
    ("java", CLASE, "params.put(\"IdPortafolio\", entrada.p_IdPortafolio);", "params.put(\"IdPortafolio\", 0L);", "the portfolio filter is ignored (FM-CPFF-06)", ""),
    ("java", CLASE, "params.put(\"IdPersonaCuentaPropia\", empresa.IdPersona);", "params.put(\"IdPersonaCuentaPropia\", entrada.p_IdEmpresa);", "the related persons are looked up with the company id instead of its person (D-F3C-14 / FM-CPFF-11)", ""),
    # ---- relation and vault (FM-CPFF-11 / 12)
    ("java", CLASE, "if(_c.SI.equals(entrada.p_AgrupaPorRelacion)) {", "if(_c.SI.equalsIgnoreCase(entrada.p_AgrupaPorRelacion)) {", "'s' groups by relation too (custody) (FM-CPFF-11)", ""),
    ("java", CLASE, "if(_c.SI.equals(entrada.p_AgrupaPorRelacion)) {", "if(_c.SI.equalsIgnoreCase(entrada.p_AgrupaPorRelacion)) {", "'s' groups by relation too (guarantee) (FM-CPFF-11)", "", 2),
    ("java", CLASE, "if(_c.SI.equals(entrada.p_AgrupaPorBoveda)) {", "if(_c.SI.equalsIgnoreCase(entrada.p_AgrupaPorBoveda)) {", "'s' groups by vault too (custody) (FM-CPFF-12)", ""),
    ("java", CLASE, "if(_c.SI.equals(entrada.p_AgrupaPorBoveda)) {", "if(_c.SI.equalsIgnoreCase(entrada.p_AgrupaPorBoveda)) {", "'s' groups by vault too (guarantee) (FM-CPFF-12)", "", 2),
    ("java", CLASE, "if(cus.IdCliente.equals(empresa.IdCliente)) relacion = \"CPRO\";", "if(!cus.IdCliente.equals(empresa.IdCliente)) relacion = \"CPRO\";", "own-account relation inverted (custody) (FM-CPFF-11)", ""),
    ("java", CLASE, "else if(hPersonaDestino.get(cus.IdPersona) != null) relacion = \"REL\";", "else if(hPersonaDestino.get(cus.IdPersona) == null) relacion = \"REL\";", "related relation inverted (custody) (FM-CPFF-11)", ""),
    ("java", CLASE, "if(gar.IdCliente.equals(empresa.IdCliente)) relacion = \"CPRO\";", "if(!gar.IdCliente.equals(empresa.IdCliente)) relacion = \"CPRO\";", "own-account relation inverted (guarantee) (FM-CPFF-11)", ""),
    ("java", CLASE, "else if(hPersonaDestino.get(gar.IdPersona) != null) relacion = \"REL\";", "else if(hPersonaDestino.get(gar.IdPersona) == null) relacion = \"REL\";", "related relation inverted (guarantee) (FM-CPFF-11)", ""),
    ("java", CLASE, "else relacion = \"TER\";", "else relacion = \"REL\";", "third party labelled REL (custody) (FM-CPFF-11)", ""),
    ("java", CLASE, "boveda = cus.IdBoveda;", "boveda = \"\";", "the vault of the custody is not used when grouping by vault (FM-CPFF-12)", ""),
    ("java", CLASE, "boveda = gar.IdBoveda;", "boveda = \"\";", "the vault of the guarantee is not used when grouping by vault (FM-CPFF-12)", ""),
    ("java", CLASE, " + \"_\" + cus.IdRegimenFondoMutuo;", ";", "the regime is not part of the custody key (FM-CPFF-12)", ""),
    ("java", CLASE, " + \"_\" + gar.IdRegimenFondoMutuo;", ";", "the regime is not part of the guarantee key (FM-CPFF-12)", ""),
    ("java", CLASE, "cus.IdInstrumento + \"_\" + cus.Nemotecnico + \"_\" + cus.IdEmisor", "cus.IdInstrumento + \"_\" + cus.IdEmisor", "the nemotecnico is not part of the custody key (FM-CPFF-12, D-F3C-13)", ""),
    ("java", CLASE, "cus.IdEmisor + \"_\" + cus.IdCliente", "cus.IdCliente", "the issuer is not part of the custody key (FM-CPFF-12)", ""),
    ("java", CLASE, "String relacion = \"\";\n\t\t\tString boveda = \"\";\n\t\t\tif(_c.SI.equals(entrada.p_AgrupaPorRelacion)) {\n\t\t\t\tif(gar.", "String relacion = \"\";\n\t\t\tString boveda = \"\";\n\t\t\tif(false) {\n\t\t\t\tif(gar.",
     "the guarantee never groups by relation (FM-CPFF-11)", ""),
    # ---- accumulation (FM-CPFF-13 / 14)
    ("java", CLASE, "custodia.Cantidad = custodia.Cantidad.add(cus.Cantidad, MathContext.DECIMAL128);", "custodia.Cantidad = custodia.Cantidad.add(cus.Cantidad, MathContext.DECIMAL32);", "quantity summed with 7 digits (FM-CPFF-13, D-F3C-11)", ""),
    ("java", CLASE, "custodia.ValorCompra = custodia.ValorCompra.add(cus.ValorCompra, MathContext.DECIMAL128);", "custodia.ValorCompra = custodia.ValorCompra.add(cus.MontoCompra, MathContext.DECIMAL128);", "the purchase value sums the rate amount (FM-CPFF-13)", ""),
    ("java", CLASE, "custodia.MontoCompra = custodia.MontoCompra.add(cus.MontoCompra, MathContext.DECIMAL128);", "custodia.MontoCompra = custodia.MontoCompra.add(cus.ValorCompra, MathContext.DECIMAL128);", "the rate amount sums the purchase value (FM-CPFF-13)", ""),
    ("java", CLASE, "custodia.PrecioCompra = cus.PrecioCompra;", "custodia.PrecioCompra = custodia.PrecioCompra;", "the price of the group is the first, not the last (FM-CPFF-13: the group price is never output)",
     "the PrecioCompra of the group is not an output field: it is only kept in the internal Custodia (FM-CPFF-13)"),
    ("java", CLASE, "cus.Cantidad.multiply(cus.PrecioCompra, MathContext.DECIMAL128)", "cus.PrecioCompra", "the price amount does not multiply by the quantity (FM-CPFF-13)", ""),
    ("java", CLASE, "cus.Cantidad.multiply(cus.PrecioCompra, MathContext.DECIMAL128),MathContext.DECIMAL128)", "cus.Cantidad.multiply(cus.PrecioCompra, MathContext.DECIMAL32),MathContext.DECIMAL128)",
     "the quantity * price product with 7 digits (FM-CPFF-13, D-F3C-11)", ""),
    ("java", CLASE, "garantia.Cantidad = garantia.Cantidad.add(gar.Cantidad, MathContext.DECIMAL128);", "garantia.Cantidad = gar.Cantidad;", "the last guarantee row replaces the sum (FM-CPFF-14)", ""),
    ("java", CLASE, "custodia.RegimenTributario = cus.RegimenTributario;", "custodia.RegimenTributario = \"\";", "the tax regime description of the custody is lost (FM-CPFF-14)", ""),
    ("java", CLASE, "custodia.Producto = cus.Producto;", "custodia.Producto = null;", "the product of the custody is lost (FM-CPFF-14)", ""),
    ("java", CLASE, "garantia.Producto = gar.Producto;", "garantia.Producto = null;", "the product of a guarantee-only group is lost (FM-CPFF-14)", ""),
    ("java", CLASE, "custodia.IdSocioDeNegocio = cus.IdSocioDeNegocio;", "custodia.IdSocioDeNegocio = null;", "the business partner of the custody is lost (FM-CPFF-14)", ""),
    ("java", CLASE, "custodia.EsADC = cus.EsADC;", "custodia.EsADC = \"N\";", "the ADC flag of the custody is lost (FM-CPFF-14)", ""),
    ("java", CLASE, "garantia.IdMatrizAgrupador = gar.IdMatrizAgrupador;", "garantia.IdMatrizAgrupador = 0L;", "the matrix of a guarantee-only group is lost (FM-CPFF-14)", ""),
    ("java", CLASE, "custodia.CodigoPortafolio = cus.CodigoPortafolio;", "custodia.CodigoPortafolio = null;", "the portfolio code of the custody is lost (FM-CPFF-14)", ""),
    ("java", CLASE, "custodia.IdTipoInstrumento = cus.IdTipoInstrumento;", "custodia.IdTipoInstrumento = null;", "the instrument type of the custody is lost (FM-CPFF-14)", ""),
    # ---- the numbers of the output (FM-CPFF-15)
    ("java", CLASE, "if (custodia.Cantidad.compareTo(BigDecimal.ZERO) == 0) {", "if (custodia.Cantidad.compareTo(BigDecimal.ZERO) <= 0) {", "a negative quantity is treated as zero: no division (FM-CPFF-15)", ""),
    ("java", CLASE, "salida.INGPrecioPromedioCompra = custodia.ValorCompra.divide(custodia.Cantidad, MathContext.DECIMAL128);", "salida.INGPrecioPromedioCompra = custodia.MontoCompra.divide(custodia.Cantidad, MathContext.DECIMAL128);",
     "the average purchase price divides the rate amount (FM-CPFF-15)", ""),
    ("java", CLASE, "salida.INGPrecioPromedioCompra = custodia.ValorCompra.divide(custodia.Cantidad, MathContext.DECIMAL128);", "salida.INGPrecioPromedioCompra = custodia.ValorCompra.divide(custodia.Cantidad, MathContext.DECIMAL64);",
     "the average purchase price with 16 digits (FM-CPFF-15, D-F3C-10)", ""),
    ("java", CLASE, "salida.INGMontoPrecioPromedioMonedaInstrumento = custodia.ValorCompra.divide(custodia.Cantidad, MathContext.DECIMAL128).multiply(custodia.Cantidad, MathContext.DECIMAL128);",
     "salida.INGMontoPrecioPromedioMonedaInstrumento = custodia.ValorCompra;", "the amount in instrument currency is X, not (X / q) * q (D-F3C-10)", ""),
    ("java", CLASE, "salida.INGMontoPrecioPromedioCompra = custodia.MontoCompra.divide(custodia.Cantidad, MathContext.DECIMAL128).multiply(custodia.Cantidad, MathContext.DECIMAL128);",
     "salida.INGMontoPrecioPromedioCompra = custodia.MontoCompra;", "the rate amount is X, not (X / q) * q (D-F3C-10)", ""),
    ("java", CLASE, "salida.MontoPrecioPromedio = salida.MontoPrecioPromedio.add(custodia.MontoPrecioPromedio);", "salida.MontoPrecioPromedio = salida.MontoPrecioPromedio.add(custodia.MontoCompra);",
     "the price amount of the output is the rate amount (FM-CPFF-15)", ""),
    ("java", CLASE, "salida.CantidadPrecioPromedio = custodia.Cantidad;", "salida.CantidadPrecioPromedio = BigDecimal.ZERO;", "the price quantity does not take the custody (FM-CPFF-15)", ""),
    ("java", CLASE, "salida.CantidadPrecioPromedio = salida.CantidadPrecioPromedio.add(garantia.Cantidad);", "salida.CantidadPrecioPromedio = salida.CantidadPrecioPromedio.add(BigDecimal.ZERO);",
     "the guarantee is not part of the divisor of the PPP (D-F3C-02)", ""),
    ("java", CLASE, "salida.MontoPrecioPromedio = BigDecimal.ZERO;\n\t\t\t\t};", "salida.MontoPrecioPromedio = salida.MontoPrecioPromedio;\n\t\t\t\t};", "the price amount survives a guarantee (D-F3C-01 'fixed')", ""),
    ("java", CLASE, "salida.CantidadGarantia = garantia.Cantidad;", "salida.CantidadGarantia = BigDecimal.ZERO;", "the guarantee quantity of the output (FM-CPFF-14)", ""),
    ("java", CLASE, "salida.TotalPosicion = salida.CantidadCustodia.add(salida.CantidadGarantia, MathContext.DECIMAL128);", "salida.TotalPosicion = salida.CantidadCustodia;", "the total position without the guarantee (FM-CPFF-15)", ""),
    ("java", CLASE, "if(salida.CantidadPrecioPromedio.compareTo(BigDecimal.ZERO) != 0) {", "if(salida.CantidadPrecioPromedio.compareTo(BigDecimal.ZERO) > 0) {", "a negative price quantity leaves the PPP null (FM-CPFF-15)", ""),
    ("java", CLASE, "salida.PPPCustodia = salida.MontoPrecioPromedio.divide(salida.CantidadPrecioPromedio, MathContext.DECIMAL128);", "salida.PPPCustodia = salida.MontoPrecioPromedio.divide(salida.CantidadPrecioPromedio, MathContext.DECIMAL64);",
     "the PPP with 16 digits (FM-CPFF-15)", ""),
    ("java", CLASE, "salida.RegimenTributario = ins.RegimenTributario;", "salida.RegimenTributario = \"\";", "the tax regime of the output (FM-CPFF-14)", ""),
    ("java", CLASE, "salida.IdBoveda = ins.IdBoveda;", "salida.IdBoveda = \"\";", "the vault of the output (FM-CPFF-12)", ""),
    ("java", CLASE, "salida.Relacion = ins.Relacion;", "salida.Relacion = \"\";", "the relation of the output (FM-CPFF-11)", ""),
    ("java", CLASE, "salida.EsADC = ins.EsADC;", "salida.EsADC = \"N\";", "the ADC flag of the output (FM-CPFF-14)", ""),
    ("java", CLASE, "salida.Nemotecnico = ins.Nemotecnico;", "salida.Nemotecnico = ins.NombreTipoInstrumento;", "the nemotecnico of the output (FM-CPFF-14)", ""),
    ("java", CLASE, "salida.IdSocioDeNegocio = ins.IdSocioDeNegocio;", "salida.IdSocioDeNegocio = null;", "the business partner of the output (FM-CPFF-14)", ""),
    ("java", CLASE, "HashMap<String,Instrumento> hInstrumento = new HashMap<String,Instrumento>();", "HashMap<String,Instrumento> hInstrumento = new java.util.LinkedHashMap<String,Instrumento>();",
     "insertion order instead of the HashMap order (FM-CPFF-16, D-F3C-13)", ""),
    ("java", CLASE, "for(Garantia gar : listGarantias) {", "for(Garantia gar : new java.util.ArrayList<Garantia>(listGarantias.subList(0, Math.min(listGarantias.size(), 1000000)))) {", "a no-op copy of the guarantee list (sanity: must NOT change a golden)",
     "no-op: the same rows in the same order"),
    # ---- the five SQL
    ("sql", SQL % CUS, "CAR.\"IdEstado\" = 'VIG'", "CAR.\"IdEstado\" <> 'ANU'", "current custody takes every state but ANU (FM-CPFF-20)", ""),
    ("sql", SQL % CUS, "AND CAR.\"IdClasificacionTipoInstrumento\" = 'FM'", "", "current custody of any classification (FM-CPFF-08)", ""),
    ("sql", SQL % CUS, ":FechaConsulta < case when", ":FechaConsulta <= case when", "the egress date is inclusive (FM-CPFF-08, QD-F3C-06)", ""),
    ("sql", SQL % CUS, ":FechaConsulta BETWEEN CAR.\"INGFechaLiquidacion\" and", ":FechaConsulta > CAR.\"INGFechaLiquidacion\" and 1=1 and :FechaConsulta <=", "the entry date is exclusive (FM-CPFF-08)", ""),
    ("sql", SQL % CUS, "THEN '2300-12-31' ELSE IFNULL(CAR.\"EGRFechaOperacion\",'0001-01-01') END\n\t\t\t\tAND", "THEN '2300-12-31' ELSE IFNULL(CAR.\"EGRFechaOperacion\",'0001-01-01') END\n\t\t\t\tAND",
     "(placeholder replaced below by the real ADQ mutation)", "no-op placeholder"),
    ("sql", SQL % CUS, "CASE WHEN IFNULL(CAR.\"INGFechaOperacion\",'0001-01-01') = '0001-01-01' THEN '2300-12-31' ELSE IFNULL(CAR.\"EGRFechaOperacion\",'0001-01-01') END",
     "CASE WHEN IFNULL(CAR.\"EGRFechaOperacion\",'0001-01-01') = '0001-01-01' THEN '2300-12-31' ELSE IFNULL(CAR.\"EGRFechaOperacion\",'0001-01-01') END", "ADQ with the column fixed: open positions appear (D-F3C-05 'fixed')", ""),
    ("sql", SQL % CUS, "CAST(IFNULL(CAR.\"INGCantidad\",0) as DECIMAL(26,8)) as \"Cantidad\"", "CAST(IFNULL(CAR.\"INGCantidad\",1) as DECIMAL(26,8)) as \"Cantidad\"", "a null quantity counts 1 (FM-CPFF-08)", "CUS.Cartera.INGCantidad is NOT NULL: not observable on the current table (the historical SQL is the one with nulls)"),
    ("sql", SQL % CUS, "AND (:IdEmisor IS NULL OR CAR.\"IdEmisor\" = :IdEmisor)", "", "current custody ignores the issuer filter (FM-CPFF-06)", ""),
    ("sql", SQL % CUS, "Serie.\"IdFamiliaFondo\" = CASE WHEN IFNULL(:IdFamiliaFondo,'') <> '' THEN IFNULL(:IdFamiliaFondo,'') ELSE Serie.\"IdFamiliaFondo\" END", "1 = 1",
     "current custody ignores the family filter AND does not drop rows without series (FM-CPFF-06)", ""),
    ("sql", SQL % CUS, "AND	CAR.\"IdCliente\" = CASE WHEN :IdCliente > 0 THEN :IdCliente ELSE CAR.\"IdCliente\" END", "AND	CAR.\"IdCliente\" = :IdCliente", "client 0 filters (FM-CPFF-06)", ""),
    ("sql", SQL % CUS, "LEFT JOIN \"BSCL.SocioNegocio.BD::SOC.SocioDeNegocio\" as SOC ON CLI.\"IdPersona\" = SOC.\"IdPersona\"", "INNER JOIN \"BSCL.SocioNegocio.BD::SOC.SocioDeNegocio\" as SOC ON CLI.\"IdPersona\" = SOC.\"IdPersona\"",
     "current custody needs a business partner (FM-CPFF-10)", ""),
    ("sql", SQL % CUS, "AND (:IdProductoAgrupador IS NULL OR MAP(MatrizAgrupador.\"IdProductoAgrupador\",NULL,0,MatrizAgrupador.\"IdProductoAgrupador\") = :IdProductoAgrupador)",
     "AND (:IdProductoAgrupador IS NULL OR MatrizAgrupador.\"IdProductoAgrupador\" = :IdProductoAgrupador)", "no product equals product 0 is lost in the custody branch (FM-CPFF-06)", ""),
    ("sql", SQL % CUS, ",'' AS \"IdBoveda\"", ",'COR' AS \"IdBoveda\"", "the request branch carries vault COR (FM-CPFF-08, FM-CPFF-22.1)", ""),
    ("sql", SQL % CUS, "Solicitudes.\"Estado\" IN ('ING', 'PEN')", "Solicitudes.\"Estado\" IN ('ING')", "PEN requests are not in transit (FM-CPFF-08)", ""),
    ("sql", SQL % CUS, "AND Solicitudes.\"FechaAceptacion\" <= :FechaConsulta", "AND Solicitudes.\"FechaAceptacion\" < :FechaConsulta", "a request accepted today is not in transit (FM-CPFF-08)", ""),
    ("sql", SQL % CUS, "AND Cartera.\"IdCliente\" = Solicitudes.\"IdCliente\"", "AND 1 = 1", "the NOT IN looks at every client (FM-CPFF-08)", ""),
    ("sql", SQL % CUS, "AND Solicitudes.\"IdCliente\" = CASE WHEN :IdCliente > 0 THEN :IdCliente ELSE Solicitudes.\"IdCliente\" END", "AND 1 = 1", "the request branch ignores the client filter (FM-CPFF-06)", ""),
    ("sql", SQL % CUS, "AND Solicitudes.\"IdPortafolio\" = CASE WHEN :IdPortafolio > 0 THEN :IdPortafolio ELSE Solicitudes.\"IdPortafolio\" END", "AND 1 = 1", "the request branch ignores the portfolio filter (FM-CPFF-06)", ""),
    ("sql", SQL % CUS, ",COALESCE (REG.\"Descripcion\",'') \n", ",'' \n", "the request branch has no tax regime description (FM-CPFF-08)", ""),
    ("sql", SQL % CUS, "'0' as \"Cantidad\"", "'5' as \"Cantidad\"", "a request carries quantity 5 (FM-CPFF-08)", ""),
    ("sql", SQL % CUS, ",Solicitudes.\"ValorCuota\" as \"ValorCompra\"", ",Solicitudes.\"Monto\" as \"ValorCompra\"", "the request purchase value is its amount (FM-CPFF-08)",
     "ValorCompra / MontoCompra of a request row are never observable: its quantity is 0 (every ING... output is 0 without dividing) and a request cannot share a group with custody rows (the NOT IN of the branch); a NULL there would need a NULL ValorCuota, a NOT NULL column"),
    ("sql", SQL % CUSH, "CAR.\"IdEstado\" <> 'ANU'", "CAR.\"IdEstado\" = 'VIG'", "historical custody takes only VIG (FM-CPFF-09, FM-CPFF-20)", ""),
    ("sql", SQL % CUSH, "WHERE RANK = 1", "WHERE RANK >= 1", "every version of a folio (FM-CPFF-09)", ""),
    ("sql", SQL % CUSH, "order by  CAR.\"FolioCartera\"", "order by  CAR.\"FolioCartera\"", "(placeholder)", "no-op placeholder"),
    ("sql", SQL % CUSH, "order by CAR.\"IdCartera\" desc", "order by CAR.\"IdCartera\" asc", "the FIRST version of a folio (FM-CPFF-09)", ""),
    ("sql", SQL % CUSH, "AND CAR.\"FechaRespaldo\" = :FechaConsulta", "AND CAR.\"FechaRespaldo\" <= :FechaConsulta", "every backup up to the date (FM-CPFF-09)", ""),
    ("sql", SQL % CUSH, "AND :IdEmisor = :IdEmisor\n", "\n", "the historical request branch has no issuer tautology: it appears with a null issuer (FM-CPFF-09, D-F3C-06)", ""),
    ("sql", SQL % CUSH, "INNER JOIN \"BSCL.SocioNegocio.BD::SOC.SocioDeNegocio\" as SOC ON CLI.\"IdPersona\" = SOC.\"IdPersona\"", "LEFT JOIN \"BSCL.SocioNegocio.BD::SOC.SocioDeNegocio\" as SOC ON CLI.\"IdPersona\" = SOC.\"IdPersona\"",
     "historical custody without a partner appears (FM-CPFF-09)", ""),
    ("sql", SQL % CUSH, "CAR.\"IdTipoInstrumento\" = 'FFM'", "CAR.\"IdTipoInstrumento\" <> 'FFM'", "historical custody of other instrument types (FM-CPFF-09)", ""),
    ("sql", SQL % CUSH, "COALESCE (CAR.\"PrecioCompra\",0) AS \"PrecioCompra\"", "CAR.\"PrecioCompra\" AS \"PrecioCompra\"", "a null purchase price is not 0 (FM-CPFF-09)", ""),
    ("sql", SQL % CUSH, "CAST(IFNULL(CAR.\"INGCantidad\",0) as DECIMAL(26,8)) as \"Cantidad\"", "CAR.\"INGCantidad\" as \"Cantidad\"", "a null quantity is not 0 (FM-CPFF-09)", ""),
    ("sql", SQL % CUSH, ",,", ",,", "(placeholder)", "no-op placeholder"),
    ("sql", SQL % GAR, "GAR.\"IdEstado\" <> 'ANU'", "GAR.\"IdEstado\" = 'VIG'", "a guarantee in any state but VIG is dropped (FM-CPFF-10)", ""),
    ("sql", SQL % GAR, "INNER JOIN \"BSCL.SocioNegocio.BD::SOC.SocioDeNegocio\" as SOC", "LEFT JOIN \"BSCL.SocioNegocio.BD::SOC.SocioDeNegocio\" as SOC", "a guarantee without a partner appears (FM-CPFF-10)", ""),
    ("sql", SQL % GAR, "CAR.\"IdBoveda\" as \"IdBoveda\"", "'' as \"IdBoveda\"", "the guarantee vault is lost (FM-CPFF-10)", ""),
    ("sql", SQL % GAR, "CAR.\"IdRegimenFondoMutuo\"", "'GEN' AS \"IdRegimenFondoMutuo\"", "the guarantee regime is always GEN (FM-CPFF-10)", ""),
    ("sql", SQL % GAR, "AND (:IdEmisor IS NULL OR CAR.\"IdEmisor\" = :IdEmisor)", "AND (:IdEmisor IS NULL OR GAR.\"IdEmisor\" = :IdEmisor)", "the issuer filter of a guarantee uses the guarantee, not the origin cartera (FM-CPFF-06)", ""),
    ("sql", SQL % GAR, "AND :FechaConsulta < CASE", "AND :FechaConsulta <= CASE", "the guarantee egress date is inclusive (FM-CPFF-10, QD-F3C-06)", ""),
    ("sql", SQL % GAR, "AND GAR.\"IdTipoInstrumento\" = 'FFM'", "", "a guarantee of any instrument type (FM-CPFF-10)", ""),
    ("sql", SQL % GAR, "INNER JOIN \"BSCL.Custodia.BD::CUS.Cartera\" as CAR ON CAR.\"IdCartera\" = GAR.\"IdCarteraOrigen\"", "LEFT JOIN \"BSCL.Custodia.BD::CUS.Cartera\" as CAR ON CAR.\"IdCartera\" = GAR.\"IdCarteraOrigen\"",
     "a guarantee without an origin cartera (FM-CPFF-10)", "the following INNER JOIN with INS.ConsultarInstrumento on CAR.IdInstrumento already drops the row"),
    ("sql", SQL % GAR, "cast(IFNULL(GAR.\"INGCantidad\",0) as DECIMAL(26,8)) as \"Cantidad\"", "GAR.\"INGCantidad\" as \"Cantidad\"", "a null guarantee quantity is not 0 (FM-CPFF-10)", "GTA.Garantia.INGCantidad is NOT NULL: not observable on the current table (the historical SQL is the one with nulls)"),
    ("sql", SQL % GARH, "AND CAR.\"FechaRespaldo\" = GAR.\"FechaRespaldo\"", "", "the origin cartera of any backup (FM-CPFF-10)", ""),
    ("sql", SQL % GARH, "AND GAR.\"FechaRespaldo\" = :FechaConsulta", "AND GAR.\"FechaRespaldo\" <= :FechaConsulta", "every guarantee backup up to the date (FM-CPFF-10)", ""),
    ("sql", SQL % GARH, "CAR.\"EGRMonto\" AS \"MontoCompraCarteraOrigen\"", "CAST(NULL AS DECIMAL(26,8)) AS \"MontoCompraCarteraOrigen\"", "the historical guarantee does not bring the origin amount (D-F3C-01)",
     "the grouped guarantee never copies MontoCompraCarteraOrigen: whatever the SQL brings is dropped (D-F3C-01): not observable at the boundary"),
    ("sql", SQL % GARH, "cast(IFNULL(GAR.\"INGCantidad\",0) as DECIMAL(26,8)) as \"Cantidad\"", "GAR.\"INGCantidad\" as \"Cantidad\"", "a null historical guarantee quantity is not 0 (FM-CPFF-10)", ""),
    ("sql", SQL % GARH, "GAR.\"IdEstado\" <> 'ANU'", "GAR.\"IdEstado\" = 'VIG'", "a historical guarantee in any state but VIG is dropped (FM-CPFF-10)", ""),
    ("sql", SQL % PER, "SELECT distinct PER.\"IdPersonaDestino\"", "SELECT PER.\"IdPersonaDestino\"", "the related persons are not DISTINCT: a repeated person raises 'key field is not unique' (FM-CPFF-11)", ""),
    ("sql", SQL % PER, "AND PER.\"IdEstado\" = 'ACT'", "", "inactive relations count (FM-CPFF-11)", ""),
]
# the two placeholders above are no-ops kept out of the totals (they would show as 'NOT APPLICABLE' or 'equivalent'): drop them
MUTACIONES = [m for m in MUTACIONES if "placeholder" not in m[4]]


def reemplazar(texto, viejo, nuevo, n):
    """Replaces the n-th occurrence (1 = the first) of `viejo`."""
    pos = -1
    for _ in range(n):
        pos = texto.index(viejo, pos + 1)
    return texto[:pos] + nuevo + texto[pos + len(viejo):]


def comparable(j):
    j = dict(j)
    j.pop("_provenance", None)
    e = j.get("excepcion")
    if e and ":" in e and e.split(":")[0] != "Exception":
        j["excepcion"] = e.split(":")[0]
    return j


def correr(script, overlay, overlay_res, salida, prefijo):
    env = dict(os.environ, ORACLE_OUT=salida.as_posix(), ORACLE_REPEAT="1", ORACLE_REPORT=(salida / "report.txt").as_posix(), ORACLE_WORK=(salida / "work").as_posix(), PYTHONUTF8="1")
    if overlay is not None:
        env["LEGACY_OVERLAY"] = overlay.as_posix()
    if overlay_res is not None:
        env["LEGACY_OVERLAY_RES"] = overlay_res.as_posix()
        # a SQL mutation needs no compilation: it reuses the classes of a previous run when ORACLE_REUSE_CLASSES points at its work dir
        if os.environ.get("ORACLE_REUSE_CLASSES"):
            env["ORACLE_WORK"] = os.environ["ORACLE_REUSE_CLASSES"]
            env["ORACLE_SKIP_COMPILE"] = "1"
    return subprocess.run([BASH, (HERE / script).as_posix(), prefijo], env=env, capture_output=True, text=True, timeout=3000)


def ejecutar(mutaciones, script, carpeta_casos, prefijo, argv):
    """Runs the mutations (filtered by argv) and prints one line per mutation; returns (total, changing, equivalents)."""
    tipo = argv[1] if len(argv) > 1 and argv[1] in ("java", "sql") else None
    filtro = argv[2] if len(argv) > 2 else (argv[1] if len(argv) > 1 and tipo is None else None)
    total = cambian = 0
    equivalentes = []
    rango = os.environ.get("CANARY_RANGE")
    for indice, mutacion in enumerate(mutaciones):
        if rango and not (int(rango.split(":")[0]) <= indice < int(rango.split(":")[1])):
            continue
        kind, archivo, viejo, nuevo, que, equiv = mutacion[:6]
        n = mutacion[6] if len(mutacion) > 6 else 1
        origen_dir = mutacion[7] if len(mutacion) > 7 else FMP
        if tipo and kind != tipo:
            continue
        if filtro and filtro not in que:
            continue
        origen = (origen_dir / archivo) if kind == "java" else (origen_dir / "sql" / archivo)
        texto = origen.read_bytes().decode("utf-8", errors="replace").replace("\r\n", "\n")
        try:
            mutado = reemplazar(texto, viejo, nuevo, n)
        except ValueError:
            print("MUTATION NOT APPLICABLE (text not found):", archivo, viejo[:70].replace("\n", " "))
            continue
        total += 1
        with tempfile.TemporaryDirectory() as t:
            t = pathlib.Path(t)
            (t / "out").mkdir()
            (t / "work").mkdir()
            if kind == "java":
                (t / "overlay").mkdir()
                (t / "overlay" / archivo).write_text(mutado, encoding="utf-8", newline="\n")
                r = correr(script, t / "overlay", None, t / "out", prefijo)
            else:
                res = t / "res" / "cl" / "bcs" / "fondomutuo" / "sql"
                res.mkdir(parents=True)
                (res / archivo).write_text(mutado, encoding="utf-8", newline="\n")
                r = correr(script, None, t / "res", t / "out", prefijo)
            if r.returncode not in (0, 4):
                print("RUN FAILED (%s %s): %s" % (kind, archivo, (r.stdout[-300:] + r.stderr[-300:]).replace("\n", " ")))
                continue
            distintos = []
            for d in sorted(carpeta_casos.glob(prefijo + "*")):
                dump = t / "out" / (d.name + ".json")
                gold = d / "expected.json"
                if not gold.exists() or not dump.exists():
                    distintos.append(d.name + " (missing)")
                    continue
                if comparable(json.loads(dump.read_text(encoding="utf-8"))) != comparable(json.loads(gold.read_text(encoding="utf-8"))):
                    distintos.append(d.name)
            if distintos:
                cambian += 1
            elif equiv:
                equivalentes.append(que)
            print("%-4s %-70s -> %3d cases change%s" % (kind, que[:70], len(distintos), "" if distintos else ("   <== EQUIVALENT (%s)" % equiv if equiv else "   <== NOT PINNED")), flush=True)
    print("mutations: %d, changing at least one golden: %d, expected equivalent (reported): %d" % (total, cambian, len(equivalentes)))
    return total, cambian, equivalentes


def main():
    ejecutar(MUTACIONES, "record_ffmm.sh", BASE / "cases-ffmm", "FM-CPFF", sys.argv)


if __name__ == "__main__":
    main()
