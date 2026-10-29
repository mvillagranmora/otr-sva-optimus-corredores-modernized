"""F2 W1B: builds seed/base-tesoreria-fm.sql, the Tesoreria master data of the layer-2 cases (synthetic, fake data):
  1. the rows of BcsTesoreriaServicios tesoreria-a/seed/base-tesoreria-a.sql (Tesoreria's own synthetic universe) for every table the FondoMutuo
     schema does NOT have (rows of the 17 shared tables are dropped: the FondoMutuo base seed owns them);
  2. the rows that make the FondoMutuo actors (person 1000 / client 100 / portfolio 500 / user 7 = employee 70 / company 1) valid for Tesoreria,
     the concept/product/operation APORFM/INFM of the movement the helper builds, Tesoreria's configuration (TES.ConfiguracionTesoreria, the values
     of Tesoreria's seed/config.json) and the two objects the helper reads that neither schema has (portfolio payment preference view, corredor
     preference). Each row says what it is for.

    py -3 seed_tesoreria_w1b.py      (after make_schema_tesoreria.py)
"""
import pathlib
import re

BASE = pathlib.Path(__file__).resolve().parent
WS = BASE.parents[3]
TES = WS / "modernized" / "otr-sva-optimus-corredores" / "BcsTesoreriaServicios" / "src" / "test" / "resources" / "tesoreria-a" / "seed" / "base-tesoreria-a.sql"

PROPIAS = [
    "-- (2) FondoMutuo actors for Tesoreria",
    'INSERT INTO "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" ("IdPersona","NombreCompleto","DocumentoIdentidad") VALUES (1000,\'Cliente Ficticio Uno\',\'1-9\');',
    'INSERT INTO "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaEstado" ("IdPersona","IdEstado") VALUES (1000,\'ACT\');',
    'INSERT INTO "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" ("IdPersona","IdCliente") VALUES (1000,100);',
    'INSERT INTO "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciasTesoreria" ("IdPortafolio","IdCliente","IdTipoCliente","TES_NIE") VALUES (500,100,\'CLI\',\'N\');',
    'INSERT INTO "BSCL.Plataforma.BD::IND.Indicador" ("IdEntidad","ValorIndicador","IdTipoIndicador","IdTipoEntidad") VALUES (100,\'N\',1,\'CLIENTE\');',
    "-- user 7 is employee 70 (ACC.Usuario of the FondoMutuo base): the employee row Tesoreria's SocioNegocioUtils / digitador lookups join to",
    'INSERT INTO "BSCL.SocioNegocio.BD::EMP.Empleado" ("IdPersona","IdEmpleado","CodigoEmpleado","IdEstado") VALUES (2070,70,\'EJ-70\',\'ACT\');',
    'INSERT INTO "BSCL.SocioNegocio.BD::EJE.Ejecutivo" ("IdEmpleadoEjecutivo","IdEstado") VALUES (70,\'ACT\');',
    "-- (2) columns Tesoreria reads that the FondoMutuo rows of the shared tables do not have (added NULL by schema-tesoreria-fm.sql)",
    'UPDATE "BSCL.Plataforma.BD::PLA.Moneda" SET "IdEstado" = COALESCE("IdEstado", \'ACT\'), "Nombre" = COALESCE("Nombre", "IdMoneda");',
    'UPDATE "BSCL.Plataforma.BD::PLA.Empresa" SET "Nombre" = COALESCE("Nombre", \'Corredora Ficticia\');',
    'UPDATE "BSCL.Portafolio.BD::POR.Portafolio" SET "IdEstado" = COALESCE("IdEstado", \'ACT\'), "CodigoPortafolio" = COALESCE("CodigoPortafolio", \'P-\' || "IdPortafolio");',
    "-- (2) movement of a FondoMutuo contribution: concept APORFM, product INFM, operation APORFM (the helper's fixed values)",
    'INSERT INTO "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto","Nombre") VALUES (\'APORFM\',\'Aporte fondo mutuo\');',
    'INSERT INTO "BSCL.Tesoreria.BD::TES.ConceptoTesoreria" ("IdConcepto","GeneraComprobanteAutomaticamente","NoValidaSaldoCaja") VALUES (\'APORFM\',\'N\',\'N\');',
    'INSERT INTO "BSCL.Plataforma.BD::INS.TipoProducto" ("IdTipoProducto") VALUES (\'INFM\');',
    'INSERT INTO "BSCL.Plataforma.BD::INS.TipoOperacion" ("IdTipoOperacion") VALUES (\'APORFM\');',
    "-- (2) Tesoreria configuration (values of Tesoreria's family-A seed/config.json). Tesoreria's stand-in table has no IdEmpresa (its oracle read the",
    "--     configuration from the case); the configuration SQL of the legacy/modern ConfiguracionSistema selects it: column added here",
    'ALTER TABLE "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria" ADD COLUMN IF NOT EXISTS "IdEmpresa" BIGINT;',
    'INSERT INTO "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria" ("IdEmpresa","CodigoParametro","Valor") VALUES (1,\'SEPARACOMPROBANTECOMSPT\',\'N\');',
    'INSERT INTO "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria" ("IdEmpresa","CodigoParametro","Valor") VALUES (1,\'LIQUIDACIONPARCIALENVIAACTA\',\'N\');',
    'INSERT INTO "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria" ("IdEmpresa","CodigoParametro","Valor") VALUES (1,\'PERMITECUENTABANCARSOCIONEGOCIO\',\'N\');',
    "-- (2) one modality per payment medium: Tesoreria's seed has CHE twice, which the legacy helper's Collectors.toMap rejects (D-TES-2, golden FMM-NOR-91)",
    'DELETE FROM "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago";',
    'INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ("IdMedioDePago","IdModalidadDePago","IdEstado") VALUES (\'TRA\',\'MODDEP\',\'ACT\');',
    'INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ("IdMedioDePago","IdModalidadDePago","IdEstado") VALUES (\'CHE\',\'MODCAJ\',\'ACT\');',
    'INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ("IdMedioDePago","IdModalidadDePago","IdEstado") VALUES (\'EFE\',\'MODCAJ\',\'ACT\');',
    'INSERT INTO "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" ("IdMedioDePago","IdModalidadDePago","IdEstado") VALUES (\'TDP\',\'MODDEP\',\'ACT\');',
    "-- (2) the two objects of ProcesarIngresoEgresoTesoreria that neither schema has (ASSUMED types, as FMM-NOR-87..96)",
    'CREATE TABLE "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio" ("IdPreferenciaPagoPortafolio" BIGINT, "IdCuentaBancaria" BIGINT, "IdBancoCuentaBancaria" BIGINT, "NumeroCuentaBancaria" NVARCHAR(100), "IdTipoCuentaBancaria" NVARCHAR(4), "NombreTipoCuentaBancaria" NVARCHAR(100));',
    'CREATE TABLE "BSCL.Tesoreria.BD::TES.PreferenciaPagoCorredor" ("IdTipoMovimiento" NVARCHAR(6), "IdConcepto" NVARCHAR(6), "IdMedioDePago" NVARCHAR(3), "IdCuentaBanco" BIGINT, "IdMoneda" NVARCHAR(3), "NombreEntidad" NVARCHAR(100), "IdEstado" NVARCHAR(3), "IdEmpresa" BIGINT);',
    'INSERT INTO "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio" VALUES (9001, 401, 1, \'0002-0002\', \'CTC\', \'Cuenta corriente\');',
    'INSERT INTO "BSCL.Tesoreria.BD::TES.PreferenciaPagoCorredor" VALUES (\'MOVEGR\', \'RESCFM\', \'TRA\', 301, \'CLP\', \'FFM.RescatePorTraspaso\', \'ACT\', 1);',
]


def main():
    fm = set(re.findall(r'CREATE (?:TABLE|VIEW|SEQUENCE) "?([^"\s(]+)"?',
                        "".join((BASE / f).read_text(encoding="utf-8") for f in ("schema-fmm.sql", "schema-session.sql"))))
    out = ["-- GENERATED by seed_tesoreria_w1b.py (do not edit; re-generate). Synthetic data only.", "-- (1) Tesoreria's family-A base seed, tables FondoMutuo does not own"]
    quitadas = 0
    for linea in TES.read_text(encoding="utf-8").split("\n"):
        m = re.match(r'INSERT INTO "([^"]+)"', linea)
        if not m:
            continue
        if m.group(1) in fm:
            quitadas += 1
            continue
        out.append(linea)
    out.extend(PROPIAS)
    (BASE / "seed" / "base-tesoreria-fm.sql").write_text("\n".join(out) + "\n", encoding="utf-8", newline="\n")
    print("rows of shared tables dropped: %d" % quitadas)


if __name__ == "__main__":
    main()
