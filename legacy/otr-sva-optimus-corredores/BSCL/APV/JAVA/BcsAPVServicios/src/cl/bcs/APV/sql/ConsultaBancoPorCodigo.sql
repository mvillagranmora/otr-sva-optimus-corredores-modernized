SELECT
	 "IdBanco"
	,"NombreBanco"
	,"CodigoBanco"
	,"IdTipoBanco"                 -- NAC / EXT
    ,"NombreTipoCampo"  -- NACIONAL / EXTRANJERO
	,"IdEstado"
	,"IdEmpresa"
	,"IdPaisOperacion"    -- Ej.: Santander(Chile)  Santander(Brasil)  > Pais operación Chile y Brazil (1 registro por cada uno)
	,"NombrePais"		
	,"NombreBancoPais"
	,"IdPersona"
	,"NombrePersona"
	,"IdSocioDeNegocio"
FROM "BSCL.Plataforma.BD.Vistas::BAN.ConsultarBanco"
WHERE "CodigoBanco" = :p_CodigoBanco;