---- 1) AGREGAR TIPO INSTRUMENTOS FALTANTES
INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento"( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('TPR','TPR','PAGARE REAJUSTABLE DE LA TESORERIA','PAGARE REAJUSTABLE DE LA TESORERIA','VIG','IF');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento"( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('MIL','AC_MIL','ACCIONES MILA','ACCIONES MILA','VIG','RV');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento"( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('BR','BR','Bono de Reconocimiento','Bono de Reconocimiento','VIG','RF');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento"( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('BH','BH','Bono Hipotecario','Bono Hipotecario','VIG','RF');
INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento"( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('PDB','PDB','PAGARE DESCONTABLE DEL BANCO CENTRAL','PAGARE DESCONTABLE DEL BANCO CENTRAL','VIG','IF');

---- 2) CREAR SCRIPT DE ACTUALIZACION DE DATOS DE TIPOS E INSTRUMENTOS FULL A QA
--select count(*) from "BSCL"."BSCL.Plataforma.BD::INS.Instrumento"		

/*

select   ' update "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" set "ISIN" = ' || '''' || INS."ISIN" || '''' ||
         ',' || '"CorteMinimo" = ' || TO_ALPHANUM(INS."CorteMinimo") ||
		 ',' || '"EstaInscrito" = ' || '''' || INS."EstaInscrito" || '''' ||
		 ',' || '"EsDivisible" = ' || '''' || INS."EsDivisible" || '''' ||
		 ',' || '"RazonSocial" = ' || '''' || INS."RazonSocial" || ''''
		 ',' || '"Lista" = ' || '''' || INS."Lista" || ''''
		 ',' || '"FechaEmision" = ' || '''' || INS."FechaEmision" || ''''
		 ',' || '"FechaVencimiento" = ' || '''' || INS."FechaVencimiento" || ''''
		 ',' || '"IdInstrumentoRelacionado" = ' || TO_ALPHANUM(INS."IdInstrumentoRelacionado") || 
		 ',' || '"IdEmisor" = ' || '''' || INS."IdEmisor" || ''''
		 ',' || '"IdTipoReajuste" = ' || '''' || INS."IdTipoReajuste" || ''''		 
		 ',' || '"Reajustable" = ' || '''' || INS."Reajustable" || ''''		 
		 ',' || '"IdMoneda" = ' || '''' || INS."IdMoneda" || ''''	
		 ',' || '"IdTipoInstrumento" = ' || '''' || INS."IdTipoInstrumento" || ''''			 
		 ',' || '"IdEstado" = ' || '''' || INS."IdEstado" || ''''	
		 ',' || '"DecimalesPrecio" = ' || TO_ALPHANUM(INS."DecimalesPrecio") || 		 
		 ',' || '"IdClasificacionRiesgo" = ' || TO_ALPHANUM(INS."IdClasificacionRiesgo") || 		 		 
		 ',' || '"Tamano" = ' || TO_ALPHANUM(INS."Tamano") || 		 		 
		 ',' || '"DiasAlVencimiento" = ' || TO_ALPHANUM(INS."DiasAlVencimiento") || 		 		 
		 ',' || '"FechaFijaCierre" = ' || '''' || INS."FechaFijaCierre" || ''''		
		 ',' || '"FechaInicioVigencia" = ' || '''' || INS."FechaInicioVigencia" || ''''		
		 ',' || '"HoraFijaCierre" = ' || '''' || INS."HoraFijaCierre" || ''''
		 ' where "Nemotecnico" = ' || '''' || INS."Nemotecnico" || ''''
		 || '&'				 		  		 
 as cadena 
 from "BSCL"."BSCL.Plataforma.BD::INS.Instrumento"	INS
	
 */
 ---- 2) Corregir IIF
----------------------------------------------------
UPDATE "BSCL"."BSCL.Plataforma.BD::INS.Instrumento"
	set   "ISIN"  		= ''
		, "CorteMinimo" = 5000000
		, "EstaInscrito"='S'
		, "EsDivisible" ='N'
		, "RazonSocial" = 'PAGARE DESCONTABLE BANCO CENTRAL'
		, "Lista"       = ''
		, "FechaEmision"= ''
		, "FechaVencimiento" = ''
		, "IdInstrumentoRelacionado"= 0
		, "IdEmisor" = 'CENTRAL'
		, "IdTipoReajuste" = 'NR'
		, "Reajustable" = 'N'
		, "IdMoneda" = 'CLP'
		, "IdTipoInstrumento" = 'PAG'---'PDB'
		, "IdEstado" = 'ACT' --AC."ESTADO"
		, "DecimalesPrecio" = 2
--from "BSCL.Plataforma.BD::INS.Instrumento" INS
where "Nemotecnico" = 'PDBC';

----------------------------------------------------
-- PRBC
UPDATE "BSCL"."BSCL.Plataforma.BD::INS.Instrumento"
	set   "ISIN"  		= ''
		, "CorteMinimo" = 0
		, "EstaInscrito"='S'
		, "EsDivisible" ='N'
		, "RazonSocial" = 'PAGARE REAJUSTABLE BANCO CENTRAL'
		, "Lista"       = ''
		, "FechaEmision"= ''
		, "FechaVencimiento" = ''
		, "IdInstrumentoRelacionado"= 0
		, "IdEmisor" = 'CENTRAL'
		, "IdTipoReajuste" = ''
		, "Reajustable" = 'S'
		, "IdMoneda" = ''
		, "IdTipoInstrumento" = 'PAG'---'PRC''PDB'
		, "IdEstado" = 'ACT' --AC."ESTADO"
		, "DecimalesPrecio" = 2
where "Nemotecnico" = 'PRBC';

INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste"("IdInstrumento", "IdTipoInstrumento", "IdTipoReajuste", "CorteMinimo")
values (2225,'PRC','UF',500);

----------------------------------------------------
-- PDT
INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento"( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('PDT','PDT','PAGARE DESCONTABLE TESORERIA','PAGARE DESCONTABLE DE LA TESORERIA','VIG','IF');

UPDATE "BSCL"."BSCL.Plataforma.BD::INS.Instrumento"
	set   "ISIN"  		= ''
		, "CorteMinimo" = 1000
		, "EstaInscrito"='S'
		, "EsDivisible" ='N'
		, "RazonSocial" = 'PAGARE DESCONTABLE DE LA TESORERIA'
		, "Lista"       = ''
		, "FechaEmision"= ''
		, "FechaVencimiento" = ''
		, "IdInstrumentoRelacionado"= 0
		, "IdEmisor" = 'TESORERIA'
		, "IdTipoReajuste" = 'NR'
		, "Reajustable" = 'N'
		, "IdMoneda" = 'CLP'
		, "IdTipoInstrumento" = 'PAG'---'PDT''PRC''PDB'
		, "IdEstado" = 'ACT' --AC."ESTADO"
		, "DecimalesPrecio" = 2
where "Nemotecnico" = 'PDT';

----------------------------------------------------------------------------
-- PRT REAJUSTABLE TESORERIA
INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento"( "IdTipoInstrumento","Codigo","Nombre","Descripcion","IdEstado","IdClasificacionTipoInstrumento" )  
VALUES('TPR','TPR','PAGARE REAJUSTABLE DE LA TESORERIA','PAGARE REAJUSTABLE DE LA TESORERIA','VIG','IF');

UPDATE "BSCL"."BSCL.Plataforma.BD::INS.Instrumento"
	set   "ISIN"  		= ''
		, "CorteMinimo" = 0
		, "EstaInscrito"='S'
		, "EsDivisible" ='N'
		, "RazonSocial" = 'PAGARE REAJUSTABLE DE LA TESORERIA'
		, "Lista"       = ''
		, "FechaEmision"= ''
		, "FechaVencimiento" = ''
		, "IdInstrumentoRelacionado"= 0
		, "IdEmisor" = 'TESORERIA'
		, "IdTipoReajuste" = ''
		, "Reajustable" = 'S'
		, "IdMoneda" = ''
		, "IdTipoInstrumento" = 'PAG'---'TPR''PDT''PRC''PDB'
		, "IdEstado" = 'ACT' --AC."ESTADO"
		, "DecimalesPrecio" = 2
where "Nemotecnico" = 'PRT';

INSERT INTO "BSCL"."BSCL.Plataforma.BD::INS.InstrumentoTipoReajuste"("IdInstrumento", "IdTipoInstrumento", "IdTipoReajuste", "CorteMinimo")
values (2227,'PRT','UF',400);

UPDATE "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento" SET "IdClasificacionTipoInstrumento" = 'IF' where "IdTipoInstrumento" in('PDB','PRC','PDT','TPR');

UPDATE "BSCL"."BSCL.Plataforma.BD::INS.Instrumento"  
	SET
	 "ISIN"  		= ''
		, "CorteMinimo" = 0
		, "EstaInscrito"='S'
		, "EsDivisible" ='N'
		, "RazonSocial" = 'PAGARE REAJUSTABLE'
		, "Lista"       = ''
		, "FechaEmision"= ''
		, "FechaVencimiento" = ''
		, "IdInstrumentoRelacionado"= 0
		, "IdEmisor" = ''
		, "IdTipoReajuste" = ''
		, "Reajustable" = 'S'
		, "IdMoneda" = ''
		, "IdTipoInstrumento" = 'PAG'
		, "IdEstado" = 'ACT' --AC."ESTADO"
		, "DecimalesPrecio" = 2
WHERE  "Nemotecnico" = 'PAGARE R';

UPDATE "BSCL"."BSCL.Plataforma.BD::INS.Instrumento"  
	SET
	 "ISIN"  		= ''
		, "CorteMinimo" = 0
		, "EstaInscrito"='S'
		, "EsDivisible" ='N'
		, "RazonSocial" = 'PAGARE REAJUSTABLE'
		, "Lista"       = ''
		, "FechaEmision"= ''
		, "FechaVencimiento" = ''
		, "IdInstrumentoRelacionado"= 0
		, "IdEmisor" = ''
		, "IdTipoReajuste" = 'NR'
		, "Reajustable" = 'N'
		, "IdMoneda" = 'CLP'
		, "IdTipoInstrumento" = 'PAG'
		, "IdEstado" = 'ACT' --AC."ESTADO"
		, "DecimalesPrecio" = 2
WHERE  "Nemotecnico" = 'PAGARE NR';

update "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" set "IdTipoInstrumento" = '' where "Nemotecnico" like 'PRC-%';

delete from "BSCL"."BSCL.Plataforma.BD::INS.TipoInstrumento" where "IdTipoInstrumento" in ('TPR','PDT','PRC','PDB');
