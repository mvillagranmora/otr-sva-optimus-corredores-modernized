CREATE LOCAL TEMPORARY TABLE #ActualizarPosicionInicial_p_DatosPosicion (
	"IdMoneda"					NVARCHAR(3)
	,"TipoInstrumento"			NVARCHAR(3)
	,"ResultadoPosicionInicial"	DECIMAL(26,8)
	,"Fecha"					NVARCHAR(10)
	,"PosicionInicial" 			DECIMAL(26,8)
	,"IdPosicionInicial"		DECIMAL(26,8)
	,"TipoCambioInicial"		DECIMAL(26,8)
	,"CierreIngresado"			DECIMAL(26,8)
	,"Estado"					NVARCHAR(3)
);