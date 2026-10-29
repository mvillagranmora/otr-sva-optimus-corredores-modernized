  SELECT  C."Valor" 			AS Rut
         ,CC."IdCliente"	
	FROM "BSCL.Normativa.BD::NOR.Configuracion"	C
	LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC ON CC."DocumentoIdentidad" = C."Valor"
   WHERE C."IdEmpresa" 			= :IdEmpresa
     AND C."CodigoParametro" 	= 'RutP40Filial'