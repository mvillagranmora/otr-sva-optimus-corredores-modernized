SELECT
     pagMes."IdFormaDePago"
    ,pagMes."Nombre" as "NombreFormaDePago"
    ,pagMes."Dias"
    ,pagMes."PorDefecto"
    ,coalesce(DICCIONARIO."Descripcion",'') as "RecibePagaPortafolioDesc"
    ,pagMes."IdEstado"
    ,estPag."Nombre" AS "NombreEstado"
    ,prefPag."IdPreferenciaPago"
    ,prefPag."IdTipoOperacion"
    ,prefPag."IdMedioDePago"
    ,prefPag."IdMoneda"
    ,prefPag."UsaPersonaRelacionada"   
    ,CAST (prefPag."FechaCreacion" AS DATE) AS  "FechaCreacion"
    ,CAST (prefPag."FechaModificacion" AS DATE) AS "FechaModificacion"
    ,prefPag."IdUsuarioCreador"
    ,prefPag."IdEmpresa"
    ,prefPag."NombreTipoOperacion"
    ,prefPag."IdTipoProducto"
    ,prefPag."NombreTipoProducto"
    ,prefPag."RecibePagaPortafolio"
    ,prefPag."NombreMedioDePago"
    ,prefPag."NombreMoneda"
    ,prefPag."UsaCuentaBancaria"
    ,prefPag."UsaTarjetaBancaria"
    ,prefPag."IdEstado" AS "IdEstadoPreferenciaPago"
    ,prefPag."NombreEstado" as "NombreEstadoPreferenciaPago"
    ,CONLIQ."IdCondicionDeLiquidacion"
    ,CONLIQ."Codigo" AS "CodigoCondicionDeLiquidacion"
    ,MODPAG."IdModalidadDePago"
FROM "BSCL.Mesa.BD::MES.FormaDePagoMesa" pagMes
INNER JOIN "BSCL.Plataforma.BD.Vistas::PAG.ConsultarPreferenciaPago" prefPag
	ON (pagMes."IdPreferenciaPago" = prefPag."IdPreferenciaPago" AND
		pagMes."IdEmpresa" = prefPag."IdEmpresa" AND
		prefPag."IdEstado" = 'ACT')
INNER JOIN  "BSCL.Plataforma.BD::PLA.Configuracion" CONF
	ON ("CodigoParametro" = 'PLA.IdiomaSistema' AND
		pagMes."IdEmpresa" = CONF."IdEmpresa")
LEFT JOIN (
		SELECT
			CASE WHEN DIC."CodDiccionario"='UI_RECIBIMOS' THEN 'R'
				 WHEN DIC."CodDiccionario"='UI_ENTREGAMOS' THEN 'P'
				 ELSE '' END AS "RecibePagaPortafolio"
			,*
		FROM "BSCL.Plataforma.BD::PLA.Diccionario" DIC
		WHERE DIC."CodDiccionario" in ('UI_RECIBIMOS','UI_ENTREGAMOS')) DICCIONARIO
	ON (prefPag."RecibePagaPortafolio" = DICCIONARIO."RecibePagaPortafolio" AND
		CONF."Valor" = DICCIONARIO."CodLenguaje")
LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" estPag
	ON (estPag."IdEstado" = pagMes."IdEstado")
LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" CONLIQ
	ON (pagMes."Dias" = CONLIQ."PlazoEnDias")
LEFT JOIN  "BSCL.Plataforma.BD::PAG.MedioDePagoYModalidadDePago" AS MODPAG ON  MODPAG."IdMedioDePago" = prefPag."IdMedioDePago"
WHERE prefPag."IdTipoProducto" IN (:IdTipoProducto) 
    --AND prefPag."IdTipoOperacion" IN (:IdTipoOperacion) 
  AND pagMes."IdEstado" = 'ACT';