INSERT INTO "BSCL.SocioNegocio.BD::CLI.LogGenerarContratos"
("Idlog","CodigoFormulario","CampoBackend","IdEmpresa", "IdCliente", "IdPersona", "IdUsuario", "FechaSistema", "IdPrograma", "Estado", "Mensaje")
SELECT "BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos".NEXTVAL, 
       'TELEFONO_', 
       'Creado', 
       :p_IdEmpresa, 
       0, 
       TEL."IdPersona", 
       :p_IdUsuario, 
       :p_FechaSistema, 
       0, 
       'PEN', 
       'Persona - ConfirmarCrearPersonaTelefonoMasivo.sql'
FROM "BSCL.SocioNegocio.BD::PER.Telefono" TEL
WHERE TEL."IdTelefono" = :p_IdTelefono;
