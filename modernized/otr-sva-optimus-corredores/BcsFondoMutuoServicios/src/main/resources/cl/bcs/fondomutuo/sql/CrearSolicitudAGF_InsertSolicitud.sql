INSERT INTO "BSCL.FondoMutuo.BD::FMM.InterfazAgf" (
"IdInterfazAgf"       , "DocumentoIdentidad" , "CodigoPortafolio"   , "FechaLiquidacion"			,
"FechaAceptacion"     , "TipoSolicitud"      , "IdEstadoSolicitud"  , "IdSerieFondo"				,
"IdUsuario"           , "NumeroCuotas"       , "ValorCuota"         , "Monto"						,
"CalculoPorMontoCuota", "IdMoneda"           , "PropositoSerie"		, "IdSerieFondoDestino"			, "IdEstadoSerieFondo", 
"IndicadorMLT"        , "IdEstado"			 , "IdEmpresa"			, "IdSolicitudAporteRescate"	, "IdSolicitudAporteRescateRelacionado"	,
"TipoRegistro"		  , "NombreCompleto"	 , "FechaIngreso"		, "FechaIngresoRegistro")
VALUES
(
"BSCL.FondoMutuo.BD.Secuencias::InterfazAgf".NEXTVAL, :DocumentoIdentidad , :CodigoPortafolio       , :FechaLiquidacion			,
:FechaAceptacion      , :TipoSolicitud       , :IdEstadoSolicitud   , :IdSerieFondo 				,
:IdUsuario            , :NumeroCuotas        , :ValorCuota          , :Monto						,
:CalculoPorMontoCuota , :IdMoneda            , :PropositoSerie		, :IdSerieFondoDestino			, :IdEstadoSerieFondo, 
:IndicadorMLT         , :IdEstado			 , :IdEmpresa			, :IdSolicitudAporteRescate	    , :IdSolicitudAporteRescateRelacionado	,
:TipoRegistro		  , :NombreCompleto	     , :FechaIngreso		, CURRENT_UTCTIMESTAMP
)