INSERT INTO BSCL."BSCL.SocioNegocio.BD::CLI.LogGenerarContratos"
("Idlog","CodigoFormulario","CampoBackend","IdEmpresa", "IdCliente", "IdPersona", "IdUsuario", "FechaSistema", "IdPrograma", "Estado", "Mensaje")
VALUES(
 "BSCL.SocioNegocio.BD.Secuencias::LogGenerarContratos".NEXTVAL
 ,'PORTAFOLIO_'
 ,'Creado'
 ,:IdEmpresa
 ,:IdCliente
 ,0
 ,:IdUsuario
 ,LEFT(:FechaIngreso,10) 
 ,0
 ,'PEN'
 ,'Cliente - CrearPortafolio.sql'
);