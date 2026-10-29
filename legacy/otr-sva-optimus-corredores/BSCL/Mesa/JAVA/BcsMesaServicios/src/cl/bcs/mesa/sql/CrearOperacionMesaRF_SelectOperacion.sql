
SELECT 	
	 OPE."IdOperacion"
	,OPE."IdEstado" 
	,TO_VARCHAR("FechaOperacion", 'YYYY-MM-DD') AS "FechaOperacion"
	,OPE."IdCliente"
	,OPE."IdPortafolio"
	,OPE."IdTipoOperacion"
	,OPE."PlazoEnDias"
	,OPE."IdUsuarioCreacion"
	,OPE."MontoOperacionInicial" 
	,RES."TotalDetalle"
	,PAG."TotalPagos"
		
	,OPE."FolioOperacion"
	,TIP_OPE."Nombre" AS "NombreTipoOperacion"
	,CASE PER_AGE."Tipo" 
		WHEN 'N' THEN PER_AGE."Nombres" ||' '|| PER_AGE."ApellidoPaterno" || ' ' ||PER_AGE."ApellidoMaterno"
		ELSE PER_AGE."RazonSocial"
	END  as "Agente"
	,IDENT."DocumentoIdentidad"
	,POR."CodigoPortafolio"
	,PROD."Nombre" AS "NombreProducto"
	,OPE."IdTipoReajuste"
	,MAIL."IdDireccionElectronica"
	,PER_CLI."IdPersona"
	,EST."Nombre" AS "NombreEstado"
	,OPE."MontoOperacionFinal"
	,OPE."MontoOperacionMoneda"
	,TO_VARCHAR(OPE."FechaVencimiento", 'YYYY-MM-DD') AS "FechaVencimiento"
		
FROM 	"BSCL.Mesa.BD::MES.Operacion" OPE
INNER JOIN "BSCL.Mesa.BD::GBL.Operacion" TMP  ON (TMP."IdOperacion" = OPE."IdOperacion")

LEFT JOIN (	SELECT 	Reserva."IdOperacion", 
					SUM("MontoValorizado") AS "TotalDetalle" 
			FROM 	"BSCL.Custodia.BD::CUS.Reserva" Reserva
			INNER JOIN "BSCL.Mesa.BD::GBL.Operacion" TMP   ON (TMP."IdOperacion" = Reserva."IdOperacion")
			GROUP BY Reserva."IdOperacion" ) RES
ON RES."IdOperacion" = OPE."IdOperacion"
LEFT JOIN (	SELECT 	Pagos."IdOperacion", 
					SUM("Monto") AS "TotalPagos" 
			FROM 	"BSCL.Mesa.BD::MES.ComprobantePagoOperacion" Pagos
			INNER JOIN "BSCL.Mesa.BD::GBL.Operacion" TMP  ON (TMP."IdOperacion" = Pagos."IdOperacion")
			GROUP BY Pagos."IdOperacion" ) PAG
ON PAG."IdOperacion" = OPE."IdOperacion"

LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" AS TIP_OPE ON (TIP_OPE."IdTipoProducto" = OPE."IdTipoProducto" AND TIP_OPE."IdTipoOperacion" = OPE."IdTipoOperacion")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER_AGE ON (PER_AGE."IdPersona" = OPE."IdAgente")
LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CLI   ON (CLI."IdCliente" = OPE."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PER_CLI   ON (PER_CLI."IdPersona" = CLI."IdPersona")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS IDENT ON (IDENT."IdDocumentoIdentidad" = PER_CLI."IdDocumentoIdentidad")
LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" AS POR ON (POR."IdPortafolio" = OPE."IdPortafolio")
LEFT JOIN "BSCL.Mesa.BD::MES.TipoProductoMesaModulo" PROD ON (PROD."IdTipoProductoMesa" = OPE."IdTipoProducto")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS MAIL ON (MAIL."IdPersona" = PER_CLI."IdPersona" AND MAIL."Predeterminado" = 'S')
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" AS EST ON  (OPE."IdEstado" = EST."IdEstado");

	