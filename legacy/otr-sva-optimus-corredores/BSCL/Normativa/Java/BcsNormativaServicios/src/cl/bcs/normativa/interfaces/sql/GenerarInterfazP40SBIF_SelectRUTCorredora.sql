SELECT  DI."DocumentoIdentidad" AS "Rut"
       ,SN."IdCliente"
  FROM "BSCL.Plataforma.BD::PLA.Empresa" EMP
  LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SN ON EMP."IdSocioDeNegocio" = SN."IdSocioDeNegocio"
  LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" P ON P."IdPersona" = SN."IdPersona"
  LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" DI ON DI."IdDocumentoIdentidad" = P."IdDocumentoIdentidad"
 WHERE EMP."IdEmpresa" = :IdEmpresa