SELECT  F."Rut" 
	   ,F."NombreFilial"
       ,CC."IdCliente"
       ,COALESCE(F."CodigoTenedor", '000') AS "CodigoTenedor"
       ,F."NombreZip"
	   ,F."NombreArchivo"
  FROM "BSCL.Normativa.BD::INT.FilialesP40" F
  LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CC ON CC."DocumentoIdentidad" = F."Rut"
  WHERE (F."Rut" = :Rut OR :Rut = '')
    AND F."IdEmpresa" = :IdEmpresa 