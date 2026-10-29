
INSERT INTO "BSCL.Plataforma.BD::EVT.Evento" 
( 
   "IdEmpresa"                  ,
   "IdEvento"                   ,
   "CodigoTipoEvento"           , 
   "IdUsuario"                  ,
   "FechaCreacion"              ,
   "Procesado"                  ,
   "TipoEntidadOrigen"          ,
   "IdEntidadOrigen"            ,
   "TipoEntidadDestino"         ,
   "IdEntidadDestino"           ,
   "IdPersonaRemitente"			,
   "IdPersonaDestinatario"		,
   "IdPortafolio"				,
   "IdOrigenEvento"
)

SELECT 
	:IdEmpresa					,
	EVE."IdEvento"				,
	EVE."CodigoTipoEvento"		,
	:IdUsuario					,
	CURRENT_UTCTIMESTAMP		,
	'N' 						,
	"TipoEntidadOrigen"         ,
  	"IdEntidadOrigen"           ,
 	"TipoEntidadDestino"        ,
   	"IdEntidadDestino"       	,
   	"IdPersonaRemitente"		,
    "IdPersonaDestinatario"		,
    "IdPortafolio"				,
    :IdOrigenEvento
   		
FROM "BSCL.Plataforma.BD::GBL.AgregarEvento_Evento" AS EVE 
INNER JOIN "BSCL.Plataforma.BD.Vistas::EVT.ConsultarDireccionEvento" TIPOEVT ON (TIPOEVT."CodigoTipoEvento" = EVE."CodigoTipoEvento")
INNER JOIN "BSCL.Plataforma.BD::EVT.ConfiguracionEventoDetalle" AS CONF	ON (CONF."CodigoTipoEvento" = EVE."CodigoTipoEvento" and CONF."IdEstado" = 'ACT');
 			
 			