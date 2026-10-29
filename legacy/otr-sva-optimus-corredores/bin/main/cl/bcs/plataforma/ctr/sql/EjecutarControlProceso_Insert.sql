-- Insert para registrar resultados de la revision de procesos

INSERT INTO "BSCL.Plataforma.BD::CTR.ControlProcesoDetalle"
	("IdControlProcesoDetalle" 
	,"IdEmpresa" 
	,"CodigoControlProcesoTipo"
	,"FechaSistema"
	,"FechaInicio"
	,"FechaTermino"
	,"CantidadTotal"
	,"CantidadEjecutada"
	,"IdEstado"
	,"Mensaje")
VALUES ("BSCL.Plataforma.BD.Secuencias::ControlProcesoDetalle".NEXTVAL
	   ,:IdEmpresa
	   ,:CodigoControlProcesoTipo
	   ,:FechaSistema
	   ,:FechaInicio
	   ,CURRENT_TIMESTAMP
	   ,:CantidadTotal
	   ,:CantidadEjecutada	   
	   ,:IdEstado
	   ,:Mensaje);