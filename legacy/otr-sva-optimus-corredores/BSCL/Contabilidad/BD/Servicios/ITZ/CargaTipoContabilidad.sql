SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Contabilidad.BD.Servicios::ITZ.CargaTipoContabilidad";
CREATE PROCEDURE
"BSCL.Contabilidad.BD.Servicios::ITZ.CargaTipoContabilidad"(
	 IN p_IdEmpresa						BIGINT
	,IN p_Ejercicio    					BIGINT
	,IN p_CantidadAsientos				INT
	--+ Tablas
	,IN p_TipoContabilidad 		TABLE ("Nombre" NVARCHAR(60),"Nemo"	NVARCHAR(20)) DEFAULT EMPTY  
	,IN p_CondicionComprobante 	TABLE ("IdTipoDatoEvento" NVARCHAR(40),"Condicion" NVARCHAR(10),"ValorVarchar"	NVARCHAR(60),"ValorDecimal"	DECIMAL(18,4)) DEFAULT EMPTY
	,IN p_DefinicionAsiento 	TABLE ("Orden" BIGINT,"IdCuentaContable" NVARCHAR(12),"DebeHaber" NVARCHAR(1)) DEFAULT EMPTY
	,IN p_DefinicionTipoEvento 	TABLE ("Orden" BIGINT,"IdTipoEvento" NVARCHAR(40),"Operador" NVARCHAR(10)) DEFAULT EMPTY
	,IN p_CondicionAsiento	 	TABLE ("Orden" BIGINT,"IdTipoDatoEvento" NVARCHAR(40),"Condicion" NVARCHAR(10),"ValorVarchar" NVARCHAR(60),"ValorDecimal" DECIMAL(18,4))DEFAULT EMPTY
	)
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER
DEFAULT SCHEMA BSCL
AS
BEGIN

	DECLARE v_IdTipoContabilidad BIGINT;
	DECLARE v_IdDefinicionAsiento BIGINT;
	DECLARE v_IdCondicion BIGINT;
	DECLARE v_Index INTEGER;

	--Tipo Contabilidad
	SELECT "BSCL.Contabilidad.BD.Secuencias::IdTipoContabilidad".NEXTVAL INTO v_IdTipoContabilidad FROM DUMMY;

	INSERT INTO "BSCL.Contabilidad.BD::ITZ.ContabilidadTipo" ("IdTipoContabilidad","Nombre","Nemo","Tipo","Reversa","Agrupa","Condicionado","TipoGeneracion","IdEmpresa","Ejercicio","Formato","Estado")
	SELECT v_IdTipoContabilidad,"Nombre","Nemo",'T','N','N','S','M',p_IdEmpresa,p_Ejercicio,'D','V'
	FROM :p_TipoContabilidad;
	
	--Condicion del Comprobante
	INSERT INTO "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoAsientoCondicion" ("IdCondicion","IdDefinicionAsiento","IdTipoContabilidad","IdTipoDatoEvento","ValorVarchar","ValorDecimal","Condicion",	"Conector","TipoCondicion","Estado")
	SELECT "BSCL.Contabilidad.BD.Secuencias::IdTipoAsientoCondicion".NEXTVAL AS "v_IdCondicion"
	,0,v_IdTipoContabilidad,"IdTipoDatoEvento","ValorVarchar","ValorDecimal","Condicion",'AND','C','V'
	FROM :p_CondicionComprobante;

	FOR v_Index IN 1 .. p_CantidadAsientos DO

		--Definicion Asientos
		SELECT "BSCL.Contabilidad.BD.Secuencias::IdDefinicionAsiento".NEXTVAL INTO v_IdDefinicionAsiento FROM DUMMY;
		
		INSERT INTO "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionAsiento" ("IdDefinicionAsiento","IdTipoContabilidad","IdCuentaContable","Ejercicio","Orden","DebeHaber","TipoEvento","Condicionado","OmiteCondicionCmbte","Estado","Formato")
		SELECT v_IdDefinicionAsiento,v_IdTipoContabilidad,"IdCuentaContable",p_Ejercicio,"Orden","DebeHaber",'S','S','N','V','D'
		FROM :p_DefinicionAsiento WHERE "Orden" = v_Index;
		
		INSERT INTO "BSCL.Contabilidad.BD::ITZ.ContabilidadDefinicionTipoEvento" ("IdTipoEvento","IdDefinicionAsiento","Operador","Estado") 
		SELECT "IdTipoEvento",v_IdDefinicionAsiento,"Operador",'V'
		FROM :p_DefinicionTipoEvento WHERE "Orden" = v_Index;
		
		INSERT INTO "BSCL.Contabilidad.BD::ITZ.ContabilidadTipoAsientoCondicion" ("IdCondicion","IdDefinicionAsiento","IdTipoContabilidad","IdTipoDatoEvento","ValorVarchar","ValorDecimal","Condicion",	"Conector","TipoCondicion","Estado")
		SELECT "BSCL.Contabilidad.BD.Secuencias::IdTipoAsientoCondicion".NEXTVAL AS "v_IdCondicion"
		,v_IdDefinicionAsiento,0,"IdTipoDatoEvento","ValorVarchar","ValorDecimal","Condicion",'AND','A','V'
		FROM :p_CondicionAsiento WHERE "Orden" = v_Index;

	END FOR;
	
END;
