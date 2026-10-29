SELECT
              PER."RazonSocial" as "CORREDORA"
             ,EMP."IdEmpresa"
             ,CONCAT(DIR."Calle" ,CONCAT(' ',CONCAT(DIR."Numero",CONCAT(' , ',CIU."NombreCiudad")))) AS "Direccion"
             ,DOC."DocumentoIdentidad"
             ,TEL."Numero" as "ATENCION_CLIENTE"
            FROM "BSCL.Plataforma.BD::PLA.Empresa" AS EMP
            left JOIN  "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" AS SOC ON SOC."IdSocioDeNegocio" = EMP."IdSocioDeNegocio"
            left JOIN  "BSCL.SocioNegocio.BD::PER.Persona" AS PER ON PER."IdPersona" = SOC."IdPersona"
            left JOIN  "BSCL.SocioNegocio.BD::PER.DireccionFisica" AS DIR ON DIR."IdPersona" = PER."IdPersona" AND DIR."Predeterminado" ='S'
            left JOIN  "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS DOC ON DOC."IdPersona" = PER."IdPersona" AND DOC."Predeterminado" ='S'
            left JOIN  "BSCL.Plataforma.BD::REG.Ciudad" AS CIU ON CIU."IdCiudad" = DIR."IdCiudad"
            LEFT JOIN BSCL."BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaTelefono" AS TEL ON TEL."IdTelefono" = PER."IdTelefonoPrincipal"
            WHERE EMP."IdEmpresa" = :p_IdEmpresa;