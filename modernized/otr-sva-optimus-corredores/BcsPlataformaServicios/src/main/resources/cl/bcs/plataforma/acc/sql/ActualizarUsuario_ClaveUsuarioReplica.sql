UPSERT "BSCL.Plataforma.BD::PLA.Replica.Usuario" 
VALUES (1,:IdEmpresa, UPPER(:CodigoUsuario), :Clave, 'ACT') 
WHERE "IdUsuario" = 1;
