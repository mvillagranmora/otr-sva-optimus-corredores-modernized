SET SCHEMA "BSCL";
GO

DROP VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarLibroInventarioDeSaldos";
GO

CREATE VIEW "BSCL.CuentaInversion.BD.Vistas::ConsultarLibroInventarioDeSaldos" AS
SELECT
	MOV."IdLibroInventarioDeSaldos"
   ,CI."IdCuentaInversion"
   ,CI."IdCliente"
   ,CI."IdPortafolio"
   ,CI."IdMoneda"
   ,CI."CodigoMoneda"
   ,CI."NombreMoneda"
   ,(CI."CodigoMoneda" ||' - '||CI."NombreMoneda") as "CodigoNombreMoneda"
   ,MOV."NombreCliente"
   ,MOV."IdCargoAbonoMovimiento"
   ,MOV."IdTipoMovimiento"
   ,MOV."IdConcepto"
   ,MOV."NombreConcepto"
   ,MOV."IdCondicionDeLiquidacion"
   ,MOV."FechaEfectivaMovimiento"
   ,CAST(CNF."Valor" as DATE) as "FechaSistema"
   ,CASE
   		WHEN DAYS_BETWEEN (TO_DATE(MOV."FechaEfectivaMovimiento", 'YYYY-MM-DD'), TO_DATE(CNF."Valor", 'YYYY-MM-DD')) < 0 THEN 0
		ELSE ABS(DAYS_BETWEEN (TO_DATE(MOV."FechaEfectivaMovimiento", 'YYYY-MM-DD'), TO_DATE(CNF."Valor", 'YYYY-MM-DD')))
	END as "AntiguedadDeuda"
   ,MOV."IdCargoAbonoMovimientoCalce"
   ,MOV."IdTipoMovimientoCalce"
   ,MOV."IdConceptoCalce"
   ,MOV."NombreConceptoCalce"
   ,MOV."IdCondicionDeLiquidacionCalce"
   ,MOV."FechaEfectivaMovimientoCalce"
   ,MOV."Monto"
   ,MOV."MontoCalce"
   ,MOV."Saldo"
   ,MOV."FechaCalce"
   ,MOV."IdPreferenciaCalceLibroInventario"
   ,MOV."Preferencia"
   ,MOV."IdEstado"
   ,MOV."IdEmpresa"
   ,MatrizAgrupador."Producto"
   ,MAP(MatrizAgrupador."IdProductoAgrupador",NULL,0,MatrizAgrupador."IdProductoAgrupador") AS "IdProductoAgrupador"
   ,Portafolio."CodigoPortafolio" ||' '||Portafolio."Proposito" as "NombrePortafolio"
FROM
(
	--Movimientos Calzados
	SELECT
		LIS."IdLibroInventarioDeSaldos"
	   ,CAMCA."IdCuentaInversion"
	   ,CAMCA."NombreCliente"
	   ,LIS."IdCargoAbonoMovimiento"
	   ,CAMCA."IdTipoMovimiento"
	   ,CAMCA."IdConcepto"
	   ,CCA."NombreConcepto"
	   ,CAMCA."IdCondicionDeLiquidacion"
	   ,CAMCA."FechaEfectivaMovimiento"
	   ,LIS."IdCargoAbonoMovimientoCalce"
	   ,CAMAB."IdTipoMovimiento" as "IdTipoMovimientoCalce"
	   ,CAMAB."IdConcepto" as "IdConceptoCalce"
	   ,CAB."NombreConcepto" as "NombreConceptoCalce"
	   ,CAMAB."IdCondicionDeLiquidacion" as "IdCondicionDeLiquidacionCalce"
	   ,CAMAB."FechaEfectivaMovimiento" as "FechaEfectivaMovimientoCalce"
	   ,LIS."Monto"
	   ,LIS."MontoCalce"
	   ,LIS."Saldo"
	   ,LIS."FechaCalce"
	   ,LIS."IdPreferenciaCalceLibroInventario"
	   ,PCLI."Preferencia"
	   ,LIS."IdEstado"
	   ,CAMCA."IdEmpresa"
	FROM "BSCL.CuentaInversion.BD::CTA.LibroInventarioDeSaldos" as LIS
	INNER JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" as CAMCA
			ON CAMCA."IdCargoAbonoMovimiento" = LIS."IdCargoAbonoMovimiento"
	INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarConceptosCuentaInversion" as CCA
			ON CCA."IdConcepto" = CAMCA."IdConcepto"
	INNER JOIN "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" as CAMAB
			ON CAMAB."IdCargoAbonoMovimiento" = LIS."IdCargoAbonoMovimientoCalce"
	INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarConceptosCuentaInversion" as CAB
			ON CAB."IdConcepto" = CAMAB."IdConcepto"
	 LEFT JOIN "BSCL.CuentaInversion.BD::CTA.PreferenciaCalceLibroInventario" as PCLI
			ON PCLI."IdPreferenciaCalceLibroInventario" = LIS."IdPreferenciaCalceLibroInventario"
	WHERE LIS."IdEstado" = 'VIG'
	UNION ALL
	--Movimientos Sin Calzar
	SELECT
		0 as "IdLibroInventarioDeSaldos"
	   ,CAM."IdCuentaInversion"
	   ,CAM."NombreCliente"
	   ,CAM."IdCargoAbonoMovimiento"
	   ,CAM."IdTipoMovimiento"
	   ,CAM."IdConcepto"
	   ,CPT."NombreConcepto"
	   ,CAM."IdCondicionDeLiquidacion"
	   ,CAM."FechaEfectivaMovimiento"
	   ,0 as "IdCargoAbonoMovimientoCalce"
	   ,'' as "IdTipoMovimientoCalce"
	   ,'' as "IdConceptoCalce"
	   ,'' as "NombreConceptoCalce"
	   ,'' as "IdCondicionDeLiquidacionCalce"
	   ,'0001-01-01' as "FechaEfectivaMovimientoCalce"
	   ,CAM."Monto"
	   ,0 as "MontoCalce"
	   ,CAM."SaldoLibroInventario" as "Saldo"
	   ,'0001-01-01' as "FechaCalce"
	   ,0 as "IdPreferenciaCalceLibroInventario"
	   ,0 as "Preferencia"
	   ,CAM."IdEstado"
	   ,CAM."IdEmpresa"
	FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS CAM
	INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarConceptosCuentaInversion" as CPT
			ON CPT."IdConcepto" = CAM."IdConcepto"
	INNER JOIN (SELECT "IdCargoAbonoMovimiento" FROM "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" WHERE "SaldoLibroInventario" > 0
				EXCEPT SELECT "IdCargoAbonoMovimiento" FROM "BSCL.CuentaInversion.BD::CTA.LibroInventarioDeSaldos") as SCC
			ON SCC."IdCargoAbonoMovimiento" = CAM."IdCargoAbonoMovimiento"
	WHERE "IdTipoMovimiento" = 'MOVCAR'
)	AS MOV
INNER JOIN "BSCL.CuentaInversion.BD.Vistas::ConsultarCuentaInversion" as CI
		ON CI."IdCuentaInversion" = MOV."IdCuentaInversion"
INNER JOIN "BSCL.Plataforma.BD::PLA.Configuracion" as CNF
		ON CNF."IdEmpresa" = MOV."IdEmpresa"
	   AND CNF."CodigoParametro" = 'PLA.FechaSistema'
 LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS Portafolio
		ON Portafolio."IdPortafolio" = CI."IdPortafolio"
	   AND Portafolio."IdCliente" = CI."IdCliente"
 LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarMatrizAgrupador" AS MatrizAgrupador
		ON MatrizAgrupador."IdMatrizAgrupadorAPV" = Portafolio."IdMatrizAgrupadorAPV"
WITH READ ONLY;
GO

GRANT SELECT ON "BSCL.CuentaInversion.BD.Vistas::ConsultarLibroInventarioDeSaldos" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
GO
