SET SCHEMA "BSCL";
DROP VIEW "BSCL.Homologacion.BD.Vistas::HomologaFMMovtosForward";

CREATE VIEW "BSCL.Homologacion.BD.Vistas::HomologaFMMovtosForward" AS
WITH 
	Cartera as (
		SELECT "FolioCartera", "IdCliente", "IdPortafolio", "FolioComprobante", "FechaComprobante", "IdOperacion", "FechaOperacion",
			   "IdTipoProducto", "IdMonedaPrincipal", "IdMonedaSecundaria", "MontoMonedaPrincipal" AS "MontoMonedaPrincipalIngreso",
			   "MontoMonedaSecundaria" AS "MontoMonedaSecundariaIngreso",
			   CASE WHEN "EGRIdTipoOperacion" = 'ANTFWD' THEN "EGRMontoMonedaPrincipal" 
					ELSE "INGMontoMonedaPrincipal" 
			   END AS "MontoMonedaPrincipalTermino",
			   CASE WHEN "EGRIdTipoOperacion" = 'ANTFWD' THEN "EGRMontoMonedaSecundaria" 
					ELSE "INGMontoMonedaSecundaria" 
			   END AS "MontoMonedaSecundariaTermino", 
			   
			   "IdFixing","FechaFixing","INGMontoMargen" AS "MontoMargen","ValorParidadCierre",
			   "IdSucursal","IdUsuario","IdAgente","IdPlaza","Glosa","FechaVencimientoOperacion",
			   
			   "ValorTipoCambioCierre","ValorTipoCambioCosto","ValorParidadCosto",
			   "INGMontoDiferido" AS "MontoDiferido","INGMontoDiferido" AS "MontoDiferido", "IdTipoOperacion"
		FROM bscl."BSCL.Custodia.BD::CUS.Cartera" 
		WHERE "IdTipoInstrumento" = 'FWD'
	)
SELECT
	 SINFO."FolioComprobante" AS FolioTransaccion					--int
	,SINFO."IdOperacion" AS FolioOperacion							--int
	,SINFO."FechaOperacion" AS FechaOperacion						--datetime
	,SINFO."FolioCartera" AS FolioCartera							--numeric
	,'' AS CodFondo					--char
	,TRIM(PER."DocumentoIdentidad") AS RutCliente					--char
	,SINFO."IdPortafolio" AS CuentaCliente							--char
	, 0 AS CodLibro					--int
	, 0 AS CodCartera				--int
	, 0 AS CodSubCartera			--int
	, 0 AS CodCartFinanciera		--int
	, 0 AS CodDepto					--int
	,SINFO."IdTipoProducto" AS CodProducto							--int
	, 0 AS CodSubProducto			--int
	,CASE WHEN SINFO."IdTipoOperacion" = 'COMFWD' THEN 'C'
		  WHEN SINFO."IdTipoOperacion" = 'VENFWD' THEN 'V'
		  ELSE ''
	  END AS CodTipoMovto											--int
	, 0 AS CodEntidad				--int
	,SINFO."IdSucursal" AS CodSucursal								--int
	,SINFO."IdMonedaPrincipal" AS CodMonPrinc						--int
	,SINFO."IdMonedaSecundaria" AS CodMonSecu						--int
	,SINFO."MontoMonedaPrincipalTermino" AS MtoMonPrinc				--decimal
	,SINFO."ValorParidadCierre" AS ParidadCierreMonPrinc			--decimal
	, 0 AS ParidadCierreMonSecu		--decimal
	, 0 AS TasaCierreForward		--decimal
	,SINFO."ValorTipoCambioCierre" AS TCCierreForward				--decimal
	,SINFO."MontoMonedaSecundariaTermino" AS MtoMonSecu				--decimal
	, 0 AS ParidadTransfMonPrinc	--decimal
	, 0 AS ParidadTransfMonSecu		--decimal
	, 0 AS TasaTransfForward		--decimal
	,SINFO."ValorTipoCambioCosto" AS TCTransfForward				--decimal
	, 0 AS ParidadHoyMonPrinc		--decimal
	, 0 AS ParidadHoyMonSecu		--decimal
	, 0 AS TCHoy					--decimal
	, 0 AS CodTCReferencia			--int
	, 0 AS TCReferencia				--decimal
	, 0 AS TasaMonPrinc				--decimal
	, 0 AS TasaMonSecu				--decimal
	, 0 AS PrecioSpot				--decimal
	,OPE."PuntosForward" AS PuntoForward							--decimal
	, 0 AS CotTasaMonPrinc			--decimal
	, 0 AS CotTasaMonSecu			--decimal
	, 0 AS CotPrecioSpot			--decimal
	, 0 AS CotPuntoForward			--decimal
	, 0 AS CotPrecioForward			--decimal
	, 0 AS SpreadTasa				--decimal
	, 0 AS ModCumplimiento			--int
	,SINFO."FechaOperacion" AS FechaInicio							--datetime
	,SINFO."IdPlaza" AS CodPlaza									--int
	,OPE."PlazoEnDias" AS Plazo										--int
	,SINFO."FechaVencimientoOperacion" AS FechaVencimiento			--datetime
	, 0 AS CodCurvaMonPrinc			--int
	, 0 AS CodCurvaMonSecu			--int
	, 0 AS MtoTransferencia			--decimal
	, 0 AS MtoComision				--decimal
	, 0 AS MtoRentabilidad			--decimal
	,'' AS IndVienenVamos			--char
	,SINFO."Glosa" AS Observaciones										--varchar
	,'' AS Origen					--varchar
	,'' AS Foliointerfaz			--varchar
	,(AG."Nombres" ||' '||AG."ApellidoPaterno") AS AgenteComercial		--varchar
	,OPE."IdEstado" AS Estado											--char
	,'' AS EstadoLimite				--char
	,'' AS EstadoConfirmador		--char
	,'' AS EstadoCtlFinanc			--char
	,SINFO."IdFixing" AS CodFixing										--int
	,SINFO."FechaFixing" AS FechaFixing									--datetime
	,'' AS IndNDF					--char
	,'' AS IndPermiteAnticipo		--char
	,'' AS Marca1					--varchar
	,'' AS Marca2					--varchar
	,'' AS Marca3					--varchar
	,'' AS Marca4					--varchar
	,'' AS Marca5					--varchar
	,'' AS TipoPago					--char
	,USU."CodigoUsuario" AS UsuarioCreador								--varchar
	,OPE."FechaCreacion" AS FechaCreacion								--datetime
	,'' AS Terminal					--char
	, 0 AS CodEventoTrans			--int
	,'' AS IndAsociaSpot			--char
	,SINFO."MontoMonedaSecundariaIngreso" AS MtoMonSecuIni				--decimal
	,SINFO."MontoMargen" AS Margen										--decimal
	, 0 AS CodSubLibro				--int
	, 0 AS CodLibro1				--int
	, 0 AS CodSubLibro1				--int
	, 0 AS CodSubSubLibro			--int
	,TO_VARCHAR(OPE."FechaLiquidacion",'YYYY-MM-DD') AS FecEfectiva		--datetime
	, 0 AS AntPlazo					--int
	, 0 AS AntParMP					--decimal
	, 0 AS AntParCLP				--decimal
	, 0 AS AntParMPCos				--decimal
	, 0 AS AntParMSCos				--decimal
	, 0 AS AntPtoParMP				--decimal
	, 0 AS AntPtoParMS				--decimal
	, 0 AS AntPtoParMPCos			--decimal
	, 0 AS AntPtoParMSCos			--decimal
	, 0 AS AntTasaMS				--decimal
	, 0 AS AntParMPEntFis			--decimal
	, 0 AS AntParMSEntFis			--decimal
	, 0 AS AntParMS					--decimal
	, 0 AS AntCodMonCmp				--int
	, 0 AS AntParMonCmp				--decimal
	, 0 AS AntPot10Ajuste			--int
	, 0 AS AntEfeValPteUniMS		--decimal
	, 0 AS AntMTMSpotMS				--decimal
	, 0 AS AntMTMMS					--decimal
	, 0 AS AntMTMCLP				--decimal
	, 0 AS AntMTMLiquidarMS			--decimal
	, 0 AS AntMTMCostoMS			--decimal
	, 0 AS AntMTMCostoCLP			--decimal
	, 0 AS AntMTMAJusteMonCmp		--decimal
	, 0 AS AntMTMAjusteCLP			--decimal
	, 0 AS AntMargenCLP				--decimal
	, 0 AS MtoLiquidacion			--decimal
	, 0 AS AntMtoMonsecu			--decimal
	,'' AS IndFWDAmericano			--char
FROM Cartera as SINFO
INNER JOIN "BSCL.Mesa.BD::MES.Operacion" AS OPE ON OPE."IdOperacion" = SINFO."IdOperacion" AND OPE."IdTipoProducto" = SINFO."IdTipoProducto"
INNER JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI ON CLI."IdCliente" = SINFO."IdCliente"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" AS PER ON PER."IdPersona" = CLI."IdPersona"
LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" AS USU ON USU."IdUsuario" = SINFO."IdUsuario"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS AG ON AG."IdPersona" = SINFO."IdAgente"
WITH READ ONLY;

GRANT SELECT ON "BSCL.Homologacion.BD.Vistas::HomologaFMMovtosForward" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
