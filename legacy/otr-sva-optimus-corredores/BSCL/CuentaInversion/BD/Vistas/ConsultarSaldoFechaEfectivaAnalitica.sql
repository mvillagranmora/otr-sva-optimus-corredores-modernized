SET SCHEMA "BSCL";
DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarSaldoFechaEfectivaAnalitica";

CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarSaldoFechaEfectivaAnalitica" AS
SELECT 
	   	"IdEjecutivo"
	   	,"NombreEjecutivo"
		,"CodigoSucursal"
 		,"NombreSucursal"
		,"IdCuentaInversion" 
        ,"CodigoCuentaInversion"
        ,"IdCliente"
        ,"NombreCliente"
        ,"DocumentoIdentidad"
        ,"IdPortafolio"
        ,"NombrePortafolio"
        ,"PropositoPortafolio"
        ,"CodigoPortafolio"
        ,"FechaSaldo"
        ,"IdMoneda"
        ,"CodigoMoneda"
        ,"NombreMoneda"
        ,"SaldoDisponible"
        ,"SaldoEnGarantia"
        ,"SaldoAPlazo"
        ,"SaldoAPlazoPactos"
        ,"SaldoAPlazoPrestamos"
        ,"SaldoRetiro"
        ,"SaldoRetenido"
        ,"Producto"
        ,"IdMatrizAgrupador"
        ,"EsADC"
        ,"UltimaOperacion"
        ,CAST(CASE WHEN "SaldoDisponible" = 0 THEN -"IdCuentaInversion" else 7*"IdCliente" + 3*"IdPortafolio" + 2*"UltimaOperacion" + 9*"IdCuentaInversion" + abs("SaldoDisponible") + "SaldoEnGarantia" + "SaldoAPlazo" + "SaldoRetiro" + "SaldoRetenido" + 8*"IdMatrizAgrupador" end AS BIGINT) AS "Checksum"
	FROM "BSCL.CuentaInversion.BD.Vistas::ConsultarSaldoCuentaInversionFechaEfectivaAnalitica" (
	PLACEHOLDER."$$p_idempresa$$" => 1,
 	PLACEHOLDER."$$p_fechaconsulta$$" => '0001-01-01'
  );

GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarSaldoFechaEfectivaAnalitica" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
