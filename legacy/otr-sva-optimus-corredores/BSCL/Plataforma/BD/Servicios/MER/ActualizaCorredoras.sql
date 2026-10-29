SET SCHEMA "BSCL";  
 
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::MER.ActualizaCorredoras";

CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::MER.ActualizaCorredoras" ( 

	IN	p_limpiaTablas						NVARCHAR(1) DEFAULT 'N',
	IN	p_IdInicial							BIGINT,
	
	OUT	p_out_Resultado						NVARCHAR(40),
	OUT	p_out_Mensaje						NVARCHAR(520)
										
)
	LANGUAGE SQLSCRIPT 
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"

AS   
BEGIN

	--limpiar
	v_CorredorasChuecas = SELECT "IdCorredora" FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" WHERE "IdPersona" IS NULL;
	DELETE FROM "BSCL.Plataforma.BD::INS.CorredoraCodigos" where "IdCorredora" IN (SELECT "IdCorredora" FROM :v_CorredorasChuecas);
	DELETE FROM "BSCL.Plataforma.BD::INS.Corredora" where "IdCorredora" IN (SELECT "IdCorredora" FROM :v_CorredorasChuecas);
		
	v_CorredorasEnroladas = SELECT corr."NombreCorto",per."IdPersona",corr."CodigoSVS"
							FROM "BSCL.Plataforma.BD::MER.CorredoraTEMP" corr
							LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" per on per."DocumentoIdentidad" = rtrim(ltrim(corr."Rut")) AND "TipoDocumentoIdentidad" = 'R'
							WHERE per."IdPersona" IS NOT NULL;
	
	v_CorredorasNuevas =	SELECT (row_number() over (order by "CodigoBCS") + :p_IdInicial) as "IdCorredora",temp."Rut",persona."IdPersona","CodigoSVS" AS "CodigoInscripcionRegulador",
							'ACT' AS "IdEstado",'N' AS "Borrado",RTRIM(LTRIM(temp."NombreCorto")) AS "NombreCorto"
							FROM "BSCL.Plataforma.BD::MER.CorredoraTEMP" temp
							left JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" per on per."DocumentoIdentidad" = rtrim(ltrim(temp."Rut"))
							left JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" persona on persona."DocumentoIdentidad" = rtrim(ltrim(temp."Rut"))
							where RTRIM(LTRIM(temp."Rut")) NOT IN (SELECT "DocumentoIdentidad" FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora" );
							
	
	
	
	INSERT INTO "BSCL.Plataforma.BD::INS.Corredora"("IdCorredora","IdPersona","CodigoInscripcionRegulador","IdEstado","Borrado","NombreCorto")
	SELECT "IdCorredora","IdPersona","CodigoInscripcionRegulador","IdEstado","Borrado","NombreCorto"
	FROM :v_CorredorasNuevas;
	
	UPDATE cc
	set cc."NombreCorto" = ce."NombreCorto",
	"CodigoInscripcionRegulador" = ce."CodigoSVS"
	from "BSCL.Plataforma.BD::INS.Corredora" cc
	INNER JOIN :v_CorredorasEnroladas ce on cc."IdPersona" = ce."IdPersona";
	
	SELECT 'v_CorredorasNuevas',* FROM :v_CorredorasNuevas;
	
	UPSERT "BSCL.Plataforma.BD::INS.CorredoraCodigos"("IdCorredora","IdBolsa","CodigoCorredora","CodigoCorredoraFix")
	SELECT corr."IdCorredora",'BCS' AS "IdBolsa","CodigoBCS" AS "CodigoCorredora", '0' || "CodigoBCS" as "CodigoCorredoraFix"
	FROM :v_CorredorasNuevas corr
	INNER JOIN "BSCL.Plataforma.BD::MER.CorredoraTEMP" temp on RTRIM(LTRIM(temp."NombreCorto")) = corr."NombreCorto";
	
	UPSERT "BSCL.Plataforma.BD::INS.CorredoraCodigos"("IdCorredora","IdBolsa","CodigoCorredora","CodigoCorredoraFix")
	SELECT corr."IdCorredora",'BEC' AS "IdBolsa","CodigoBEC" AS "CodigoCorredora", '' as "CodigoCorredoraFix"
	FROM :v_CorredorasNuevas corr
	INNER JOIN "BSCL.Plataforma.BD::MER.CorredoraTEMP" temp on RTRIM(LTRIM(temp."NombreCorto")) = corr."NombreCorto"
	WHERE "CodigoBEC" != '0';
	
	UPSERT "BSCL.Plataforma.BD::INS.CorredoraCodigos"("IdCorredora","IdBolsa","CodigoCorredora","CodigoCorredoraFix")
	SELECT corr."IdCorredora",'BCV' AS "IdBolsa","CodigoVALPO" AS "CodigoCorredora", '' as "CodigoCorredoraFix"
	FROM :v_CorredorasNuevas corr
	INNER JOIN "BSCL.Plataforma.BD::MER.CorredoraTEMP" temp on RTRIM(LTRIM(temp."NombreCorto")) = corr."NombreCorto"
	WHERE "CodigoVALPO" != '0';
	
	
	--SELECT 'v_CorredorasChuecas',* from :v_CorredorasChuecas;
	--SELECT 'v_CorredorasEnroladas',* FROM :v_CorredorasEnroladas;
	--SELECT 'v_CorredorasNuevas',* FROM :v_CorredorasNuevas;
	--SELECT 'v_CorredorasPersona',* FROM :v_CorredorasPersona;
	--CALL "BSCL.Plataforma.BD.Servicios::MER.ActualizaCorredoras"('N',?,?)
END;

/*UPSERT "BSCL.Plataforma.BD::INS.CorredoraCodigos"("IdCorredora","IdBolsa","CodigoCorredora","CodigoCorredoraFix")
	SELECT corr."IdCorredora",'BCS' AS "IdBolsa","CodigoBCS" AS "CodigoCorredora", '0' || "CodigoBCS" as "CodigoCorredoraFix"
	FROM :v_CorredorasNuevas corr
	INNER JOIN "BSCL.Plataforma.BD::MER.CorredoraTEMP" temp on temp."NombreCorto" = corr."NombreCorto";

CALL "BSCL.Plataforma.BD.Servicios::MER.ActualizaCorredoras"('N',200,?,?)
SELECT * FROM "BSCL.Plataforma.BD::INS.Corredora";
delete from"BSCL.Plataforma.BD::INS.Corredora"  where "IdCorredora" > 100
SELECT * FROM "BSCL.Plataforma.BD::MER.CorredoraTEMP";
SELECT * FROM "BSCL.Plataforma.BD::INS.CorredoraCodigos";
SELECT * FROM "BSCL.Plataforma.BD.Vistas::INS.ConsultarCorredora";
rollback;

SELECT * FROM "BSCL.Plataforma.BD::INS.Corredora"

IdCorredora;CodigoInscripcionRegulador;IdEstado;NombreCorto;NombreCompleto;IdPersona;DocumentoIdentidad
1;XX;ACT;;CREDICORP CAPITAL S.A. CORREDORES DE BOLSA;20.176;96489000-5
2;XX;ACT;;LARRAIN VIAL S.A. CORREDORA DE BOLSA;20.196;80537000-9

select * from "BSCL"."BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" where "DocumentoIdentidad" = '22246160-k'
IdDocumentoIdentidad;IdPersona;IdTipoDocumentoIdentidad;DocumentoIdentidad;NumeroSerie;FechaEmision;FechaVencimiento;IdEstado;FechaIngreso;FechaCreacion;FechaModificacion;IdCarga;IdEstadoAutorizacion;IdOrigen;Borrado;IdUsuarioCreador
132.765;20.044;R;22246160-k;;01-01-0001;01-01-0001;ACT;08-03-2016 21:26:34.0;08-03-2016 21:26:34.0;08-03-2016 21:26:34.0;0;ING;0;N;3

update "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" set "DocumentoIdentidad" = '96519800-8' where "DocumentoIdentidad" = '22246160-k'
update "BSCL"."BSCL.Plataforma.BD::INS.Corredora" set "NombreCorto" = RTRIM(LTRIM("NombreCorto"))
commit;*/
