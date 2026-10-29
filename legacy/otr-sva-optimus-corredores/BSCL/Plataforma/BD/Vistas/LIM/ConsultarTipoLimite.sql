SET SCHEMA "BSCL"; 
 
DROP VIEW  "BSCL.Plataforma.BD.Vistas::LIM.ConsultarTipoLimite"; 
 
CREATE VIEW "BSCL.Plataforma.BD.Vistas::LIM.ConsultarTipoLimite" 
AS
SELECT TLIM."IdTipoLimite", 
    TLIM."IdEmpresa",                   
    TLIM."Nombre",
    TLIM."EsCuentaPropia",
    TLIM."IdComportamiento", --Requiere autorizacion, Advertencia, Warning  
    TLIM."IdGrupoLimite", -- Clasificacion (Mercado) RV, MO, RF
    TLIM."Categoria", -- de POSICION O CARTERA, de OPERACION, de MONTO OPERADO
	TLIM."IdTipoEvento",
	TLIM."Prioridad",
    TLIM."IdEstado"
FROM "BSCL.Plataforma.BD::LIM.TipoLimite" TLIM;


GRANT SELECT ON "BSCL.Plataforma.BD.Vistas::LIM.ConsultarTipoLimite" TO BCS_DESARROLLADOR WITH GRANT OPTION; 
