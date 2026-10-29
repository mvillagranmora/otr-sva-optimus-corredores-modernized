
-- OBTIENE LA FORMA DE PAGO DEL PORTAFOLIO PARA CC, USANDO LOS SIGUIENTE CRITERIOS:
-- 1. Busca preferencias de pago del portafolio
-- 2. Si no hay preferencis verifica si opera con cuenta inversion
-- 3. Si no opera con cta inversion, utiliza medio de pago por defecto

SELECT 
	POR."IdPortafolio"
	,'E'	AS "IdTipoMovimiento"
	
	,CASE
		WHEN COALESCE(PPP."IdMedioDePago",'')   <> '' THEN PPP."IdMedioDePago"  -- Medio de Pago configurado en preferencias del cliente 
		WHEN COALESCE(OCIN."ValorIndicador",'') = 'S' THEN 'CIN'                -- Si opera con cuenta inversión 
		ELSE COALESCE(CONF."Valor",'TRA')                                       -- Medio de Pago por defecto
	END  AS "IdMedioDePago"

	,CASE
		WHEN COALESCE(PPP."IdMedioDePago",'')   <> '' THEN MDP."IdModalidadDePago" 
		WHEN COALESCE(OCIN."ValorIndicador",'') = 'S' THEN '' 
		ELSE COALESCE(MDPC."IdModalidadDePago",'MODDEP')
	END  AS "IdModalidadPago"

	,CASE
		WHEN COALESCE(PPP."IdMedioDePago",'')   <> '' THEN PPP."MedioDePago" 
		WHEN COALESCE(OCIN."ValorIndicador",'') = 'S' THEN 'CUENTA INVERSION' 
		ELSE COALESCE(MED."Nombre",'TRANSFERENCIA')
	END  AS "NombreMedioDePago"

	,CASE
		WHEN COALESCE(PPP."IdMedioDePago",'')   <> '' THEN PPP."IdBancoCuentaBancaria" 
		WHEN COALESCE(OCIN."ValorIndicador",'') = 'S' THEN 0 
		ELSE 0
	END  AS "IdBanco"

	,CASE
		WHEN COALESCE(PPP."IdMedioDePago",'')   <> '' THEN PPP."NombreBancoCuentaBancaria" 
		WHEN COALESCE(OCIN."ValorIndicador",'') = 'S' THEN '' 
		ELSE ''
	END  AS "NombreBanco"
	
	,CASE
		WHEN COALESCE(PPP."IdMedioDePago",'')   <> '' THEN PPP."IdTipoCuentaBancaria" 
		WHEN COALESCE(OCIN."ValorIndicador",'') = 'S' THEN '' 
		ELSE ''
	END  AS "IdTipoCuentaBancaria"
	
	,CASE
		WHEN COALESCE(PPP."IdMedioDePago",'')   <> '' THEN PPP."NombreTipoCuentaBancaria" 
		WHEN COALESCE(OCIN."ValorIndicador",'') = 'S' THEN '' 
		ELSE ''
	END  AS "NombreTipoCuentaBancaria"
	
	,CASE
		WHEN COALESCE(PPP."IdMedioDePago",'')   <> '' THEN PPP."IdCuentaBancaria" 
		WHEN COALESCE(OCIN."ValorIndicador",'') = 'S' THEN 0
		ELSE 0
	END  AS "IdCuentaBancaria"
	
	,''														AS "NombreCuentaBancaria"

	,CASE
		WHEN COALESCE(PPP."IdMedioDePago",'')   <> '' THEN PPP."NumeroCuentaBancaria" 
		WHEN COALESCE(OCIN."ValorIndicador",'') = 'S' THEN '' 
		ELSE ''
	END  AS "NumeroCuentaBancaria"

	,CASE
		WHEN COALESCE(PPP."IdMedioDePago",'')   <> '' THEN PPP."IdTipoTarjetaBancaria" 
		WHEN COALESCE(OCIN."ValorIndicador",'') = 'S' THEN '' 
		ELSE ''
	END  AS "IdTipoTarjetaBancaria"

	,CASE
		WHEN COALESCE(PPP."IdMedioDePago",'')   <> '' THEN PPP."NumeroTarjetaBancaria" 
		WHEN COALESCE(OCIN."ValorIndicador",'') = 'S' THEN '' 
		ELSE ''
	END  AS "NumeroTarjetaBancaria"	
	
	,'' 																	AS "NumeroMedioDePago"
	,CURRENT_UTCTIMESTAMP 													AS "FechaHoraIngresoRegistro"
	,'VIG'																	AS "IdEstado"

FROM "BSCL.Portafolio.BD::POR.Portafolio" AS POR
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio"   AS PPP   ON (PPP."IdPortafolio" = POR."IdPortafolio" AND PPP."IdTipoOperacion" = 'CORCUP' AND PPP."PorDefecto" = 'S' AND PPP."IdEstado" = 'ACT')
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PAG.ConsultarModalidadDePago"         AS MDP   ON (MDP."IdMedioDePago" = PPP."IdMedioDePago")

LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"   AS OCIN  ON (OCIN."CodigoTipoIndicador" = 'GEN_OCCINV' AND OCIN."ValorIndicador"='S' AND OCIN."IdPortafolio" = POR."IdPortafolio")
LEFT JOIN "BSCL.Custodia.BD::CUS.Configuracion"                             AS CONF  ON (CONF."CodigoParametro" = 'CUS.MedioPagoDefectoEVCAP' AND CONF."IdEmpresa" = :IdEmpresa)
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PAG.ConsultarModalidadDePago"         AS MDPC  ON (MDPC."IdMedioDePago" = CONF."Valor")
LEFT JOIN "BSCL.Plataforma.BD::PAG.MedioDePago" 					        AS MED   ON (CONF."Valor" = MED."IdMedioDePago")
WHERE POR."IdPortafolio" IN (:IdPortafolio);
