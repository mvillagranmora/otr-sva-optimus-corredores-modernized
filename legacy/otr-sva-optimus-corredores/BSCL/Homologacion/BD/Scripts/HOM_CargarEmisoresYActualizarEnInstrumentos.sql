------------------------------- ANALISIS -----------------------------------
select --ACMI."NEMO", 
             distinct ACMI."NEMO_SASA", asoc."RUT",  utemis."EMISOR", 
             utemis.RAZON_SOCIAL, usu.NEMO, 
             usu.RAZON_SOCIAL as USU
--           count(*)
from "BSCL"."RUE_TBACMI" as ACMI
inner join "BSCL"."RUE_TBASSOC"  as asoc on ltrim(rtrim(asoc."NEMO_SASA")) = ltrim(rtrim(ACMI."NEMO_SASA")) 
inner join "BSCL"."RUE_TBUTEMIS" as utemis on ltrim(rtrim(utemis."RUT")) = ltrim(rtrim(asoc."RUT")) 
inner join "BSCL"."RUE_TBUTUSUA" as usu on ltrim(rtrim(utemis."RUT")) = ltrim(rtrim(usu."RUT_CORR")) 
left join bscl."BSCL.Mercado.BD::MER.Emisor" as EMI on ltrim(rtrim(utemis."EMISOR")) = ltrim(rtrim(EMI."IdEmisor"))
where --ACMI."NEMO_SASA" <> utemis."EMISOR" and 
usu.ESTADO = 'V'
--and utemis.RAZON_SOCIAL like '%CONO%'
order by 2,3
--------- CARGA 0 SE TOMA LA UTEMIS Y VACIA EN TABLA EMISORES LOCAL

--------- CARGA 1)  Cargar EMISOR donde el nemo_sasa sea igual al nemo_sasa de la UTEMIS
--281 NEMOS
SELECT --count(*)
 ACMI."NEMO_SASA",utemis."EMISOR", ACMI."NEMO",INS."Nemotecnico",INS."IdEmisor", EMI."IdEmisor", * 
from "BSCL"."RUE_TBACMI" as ACMI 
		inner join "BSCL"."RUE_TBUTEMIS" as utemis on (ltrim(rtrim(utemis."EMISOR")) = ltrim(rtrim(ACMI."NEMO_SASA")))
		inner join "BSCL"."BSCL.Mercado.BD::MER.Emisor" as EMI ON ( ltrim(rtrim(EMI."IdEmisor")) = ltrim(rtrim(utemis."EMISOR")) ) --380
		inner join "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" as INS ON ( ltrim(rtrim(INS."Nemotecnico")) = ltrim(rtrim(ACMI."NEMO")) )
where INS."IdEmisor" = '';


	update "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" 
	  set "IdEmisor" = EMI."IdEmisor"
	from "BSCL"."RUE_TBACMI" as ACMI 
			inner join "BSCL"."RUE_TBUTEMIS" as utemis on (ltrim(rtrim(utemis."EMISOR")) = ltrim(rtrim(ACMI."NEMO_SASA")))
			inner join "BSCL"."BSCL.Mercado.BD::MER.Emisor" as EMI ON ( ltrim(rtrim(EMI."IdEmisor")) = ltrim(rtrim(utemis."EMISOR")) ) --380
			inner join "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" as INS ON ( ltrim(rtrim(INS."Nemotecnico")) = ltrim(rtrim(ACMI."NEMO")) )		
	where INS."IdEmisor" = ''
	and  "BSCL"."BSCL.Plataforma.BD::INS.Instrumento"."IdInstrumento" = INS."IdInstrumento";


--------- CARGA 2)  Cargar EMISOR donde el nemo_sasa no corresponde al ID Emisor de la UTEMIS.
---------           Al hacer match con acmi y tbassoc se busca el rut, con ese rut se llega a utemis y obtiene el nemo correcto
---------          pero arroja muchos nemos asociados al mismo rut, aleatoriamente se toma uno y lleva al instrumento (CORREGIR CRITERIO!!!!!, existen nemos de emisores que no corresponden
---------          por ejemplo el BHIFF asociado a instrumentos de BBVA
--23 NEMOS
SELECT Distinct INS."Nemotecnico" --utu."NEMO", utu."RAZON_SOCIAL", ACMI."NEMO_SASA",ACMI."ESTADO", asoc."NEMO" as nemo_Asoc, asoc."RUT", ut2."EMISOR", ACMI."NEMO",INS."Nemotecnico",INS."IdEmisor", EMI."IdEmisor"
from "BSCL"."RUE_TBACMI" as ACMI 
		inner join "BSCL"."RUE_TBASSOC"   as asoc   on ltrim(rtrim(asoc."NEMO_SASA")) = ltrim(rtrim(ACMI."NEMO_SASA"))
		inner join "BSCL"."RUE_TBUTEMIS"  as ut2    on ltrim(rtrim(asoc."RUT")) = ltrim(rtrim(ut2."RUT")) 	 	
		inner join "BSCL"."RUE_TBUTUSUA"  as utu    on ltrim(rtrim(asoc."RUT")) = ltrim(rtrim(utu."RUT_CORR")) 	 	
		inner join "BSCL"."BSCL.Mercado.BD::MER.Emisor" as EMI ON  ltrim(rtrim(EMI."IdEmisor")) = ltrim(rtrim(ut2."EMISOR")) 
		inner join "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" as INS ON  ltrim(rtrim(INS."Nemotecnico")) = ltrim(rtrim(ACMI."NEMO")) 
where INS."IdEmisor" = ''

--- primero los no vigentes
-- 19 nemos inactivos, coincidencia 119
SELECT Distinct INS."Nemotecnico"
from "BSCL"."RUE_TBACMI" as ACMI 
		inner join "BSCL"."RUE_TBASSOC"   as asoc   on ltrim(rtrim(asoc."NEMO_SASA")) = ltrim(rtrim(ACMI."NEMO_SASA"))
		inner join "BSCL"."RUE_TBUTEMIS"  as ut2    on ltrim(rtrim(asoc."RUT")) = ltrim(rtrim(ut2."RUT")) 	 	
		inner join "BSCL"."RUE_TBUTUSUA"  as utu    on ltrim(rtrim(asoc."RUT")) = ltrim(rtrim(utu."RUT_CORR")) 	 	
		inner join "BSCL"."BSCL.Mercado.BD::MER.Emisor" as EMI ON  ltrim(rtrim(EMI."IdEmisor")) = ltrim(rtrim(ut2."EMISOR")) 
		inner join "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" as INS ON  ltrim(rtrim(INS."Nemotecnico")) = ltrim(rtrim(ACMI."NEMO")) 
where INS."IdEmisor" = ''
and ACMI."ESTADO" = 4;--19

--- luego los vigentes
update "BSCL"."BSCL.Plataforma.BD::INS.Instrumento"
	set  "IdEmisor" = EMI."IdEmisor"
from "BSCL"."RUE_TBACMI" as ACMI 
		inner join "BSCL"."RUE_TBASSOC"   as asoc   on ltrim(rtrim(asoc."NEMO_SASA")) = ltrim(rtrim(ACMI."NEMO_SASA"))
		inner join "BSCL"."RUE_TBUTEMIS"  as ut2    on ltrim(rtrim(asoc."RUT")) = ltrim(rtrim(ut2."RUT")) 	 	
		inner join "BSCL"."RUE_TBUTUSUA"  as utu    on ltrim(rtrim(asoc."RUT")) = ltrim(rtrim(utu."RUT_CORR")) 	 	
		inner join "BSCL"."BSCL.Mercado.BD::MER.Emisor" as EMI ON  ltrim(rtrim(EMI."IdEmisor")) = ltrim(rtrim(ut2."EMISOR")) 
		inner join "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" as INS ON  ltrim(rtrim(INS."Nemotecnico")) = ltrim(rtrim(ACMI."NEMO")) 
where INS."IdEmisor" = ''
and ACMI."ESTADO" = 4
and  "BSCL"."BSCL.Plataforma.BD::INS.Instrumento"."IdInstrumento" = INS."IdInstrumento";

---- CASO 3) uno a uno para instrumentos vigentes, eligiendo a criterio el emisor que le corresponda

SELECT --Distinct INS."Nemotecnico"
utu."NEMO", utu."RAZON_SOCIAL", ACMI."NEMO_SASA",ACMI."ESTADO", asoc."NEMO" as nemo_Asoc, asoc."RUT", ut2."EMISOR", ACMI."NEMO",INS."Nemotecnico",INS."IdEmisor", EMI."IdEmisor",
 EMI."RazonSocial",EMI."CodigoSVS" 
from "BSCL"."RUE_TBACMI" as ACMI 
		inner join "BSCL"."RUE_TBASSOC"   as asoc   on ltrim(rtrim(asoc."NEMO_SASA")) = ltrim(rtrim(ACMI."NEMO_SASA"))
		inner join "BSCL"."RUE_TBUTEMIS"  as ut2    on ltrim(rtrim(asoc."RUT")) = ltrim(rtrim(ut2."RUT")) 	 	
		inner join "BSCL"."RUE_TBUTUSUA"  as utu    on ltrim(rtrim(asoc."RUT")) = ltrim(rtrim(utu."RUT_CORR")) 	 	
		inner join "BSCL"."BSCL.Mercado.BD::MER.Emisor" as EMI ON  ltrim(rtrim(EMI."IdEmisor")) = ltrim(rtrim(ut2."EMISOR")) 
		inner join "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" as INS ON  ltrim(rtrim(INS."Nemotecnico")) = ltrim(rtrim(ACMI."NEMO")) 
where INS."IdEmisor" = ''
and ACMI."ESTADO" = 1; -- 4

select * from "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" where "Nemotecnico" in( 'SUD AMER-A', 'BINT', 'BBVACL','BSANTANDER','ABNAMRO')

update "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" 
	set  "IdEmisor" = 'SCOTIABANK'
where "Nemotecnico" = 'SUD AMER-A' 

update "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" 
	set  "IdEmisor" = 'BBINTERNAC'
where "Nemotecnico" = 'BINT' 

update "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" 
	set  "IdEmisor" = 'BBBVA'
where "Nemotecnico" = 'BBVACL' 

update "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" 
	set  "IdEmisor" = 'SANTANDER'
where "Nemotecnico" = 'BSANTANDER' 

update "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" 
	set  "IdEmisor" = 'ABN AMRO'
where "Nemotecnico" = 'ABNAMRO' 

----- CASO 4 todos los pendientes que no estan directo en utemis, entramos por rut desde asoc sin la UTUSUA

SELECT ACMI."NEMO_SASA",ACMI."ESTADO", asoc."NEMO" as nemo_Asoc, asoc."RUT", ut2."EMISOR", ACMI."NEMO",INS."Nemotecnico",INS."IdEmisor", EMI."IdEmisor"
from "BSCL"."RUE_TBACMI" as ACMI 
		inner join "BSCL"."RUE_TBASSOC"   as asoc   on ltrim(rtrim(asoc."NEMO_SASA")) = ltrim(rtrim(ACMI."NEMO_SASA"))
		inner join "BSCL"."RUE_TBUTEMIS"  as ut2    on ltrim(rtrim(asoc."RUT")) = ltrim(rtrim(ut2."RUT")) 	 	
		inner join "BSCL"."BSCL.Mercado.BD::MER.Emisor" as EMI ON  ltrim(rtrim(EMI."IdEmisor")) = ltrim(rtrim(ut2."EMISOR")) 
		inner join "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" as INS ON  ltrim(rtrim(INS."Nemotecnico")) = ltrim(rtrim(ACMI."NEMO")) 
where INS."IdEmisor" = '';

update "BSCL"."BSCL.Plataforma.BD::INS.Instrumento"
	set  "IdEmisor" = EMI."IdEmisor"
from "BSCL"."RUE_TBACMI" as ACMI 
		inner join "BSCL"."RUE_TBASSOC"   as asoc   on ltrim(rtrim(asoc."NEMO_SASA")) = ltrim(rtrim(ACMI."NEMO_SASA"))
		inner join "BSCL"."RUE_TBUTEMIS"  as ut2    on ltrim(rtrim(asoc."RUT")) = ltrim(rtrim(ut2."RUT")) 	 	
		inner join "BSCL"."BSCL.Mercado.BD::MER.Emisor" as EMI ON  ltrim(rtrim(EMI."IdEmisor")) = ltrim(rtrim(ut2."EMISOR")) 
		inner join "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" as INS ON  ltrim(rtrim(INS."Nemotecnico")) = ltrim(rtrim(ACMI."NEMO")) 
where INS."IdEmisor" = ''
and  "BSCL"."BSCL.Plataforma.BD::INS.Instrumento"."IdInstrumento" = INS."IdInstrumento";

---- CASO 5) crear emisores que no estan en utemis y si estan en asoc

INSERT INTO "BSCL"."BSCL.Mercado.BD::MER.Emisor" ( "IdEmisor","Codigo","Nombre","CodigoSVS","RazonSocial","TipoEntidad","IdEmisorYDocumentoIdentidadDefecto","IdEstado" )  
--VALUES('A. PARIS','G4','','PARI','EMPRESAS ALMACENES PARIS S.A.','3',1,'ACT');
select  DISTINCT ltrim(rtrim(ACMI."NEMO_SASA")) as "IdEmisor"
		,''  as "Codigo"
		,''  as "Nombre"
		,''  as "CodigoSVS"
		--,ACMI."RAZON_SOCIAL" as "RazonSocial"
		,ltrim(rtrim(ACMI."NEMO_SASA")) as "RazonSocial"
		,'3' 				as "TipoEntidad" -- REVISAR
		, 1 AS "IdEmisorYDocumentoIdentidadDefecto"
		,'ACT' AS "IdEstado" 
	from "BSCL"."RUE_TBACMI" as ACMI 
	inner join "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" as INS ON  ltrim(rtrim(INS."Nemotecnico")) = ltrim(rtrim(ACMI."NEMO")) 
	inner join "BSCL"."RUE_TBASSOC"   as asoc   on ltrim(rtrim(asoc."NEMO_SASA")) = ltrim(rtrim(ACMI."NEMO_SASA"))
	left  join "BSCL"."RUE_TBUTEMIS"   as emis   on ltrim(rtrim(emis."RUT")) = ltrim(rtrim(asoc."RUT"))	
where INS."IdEmisor" = '' and INS."IdTipoInstrumento" ='ACN'
and emis."RUT" is null
ORDER BY 1

-- 312
update "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" 
	  set "IdEmisor" = EMI."IdEmisor"
from "BSCL"."RUE_TBACMI" as ACMI 
		inner join "BSCL"."BSCL.Mercado.BD::MER.Emisor" as EMI ON ( ltrim(rtrim(EMI."IdEmisor")) = ltrim(rtrim(ACMI."NEMO_SASA")) ) --380
		inner join "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" as INS ON ( ltrim(rtrim(INS."Nemotecnico")) = ltrim(rtrim(ACMI."NEMO")) )		
where INS."IdEmisor" = '' and INS."IdTipoInstrumento" IN('ACN','ACX')
and  "BSCL"."BSCL.Plataforma.BD::INS.Instrumento"."IdInstrumento" = INS."IdInstrumento";
	
---- NEMO_SASA DE LA ACMI NO CUADRA CON NADA!!! uno a uno buscar en ASOC
select * from "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" WHERE "IdEmisor" = '' and "IdTipoInstrumento" IN('ACN')

SELECT "NEMO", "NEMO_SASA", * from "BSCL"."RUE_TBACMI" as ACMI where "NEMO"='CHILET-OSA'
SELECT * from  "BSCL"."RUE_TBASSOC"  where "NEMO_SASA" like '%CHILE%' 
SELECT * from "BSCL"."RUE_TBUTEMIS"  where "EMISOR" like '%CDCRAIG%' 
SELECT * from "BSCL"."BSCL.Mercado.BD::MER.Emisor"  where "IdEmisor" like '%CHILE%' 
select * from "BSCL"."BSCL.Mercado.BD::MER.Emisor" where "IdEmisor" = 'CDCRAIG'

select * from "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" WHERE "IdEmisor" = '' and "IdTipoInstrumento" IN('ACN') AND "RazonSocial" like '%CDCRA%'
select * from "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" WHERE "IdEmisor" = '' and "IdTipoInstrumento" IN('ACN') AND "Nemotecnico" like '%CHILE%'


update "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" set "IdEmisor" = 'ESSBIO' WHERE "IdEmisor" = '' and "IdTipoInstrumento" IN('ACN') AND "RazonSocial" like '%ESSBIO%'
update "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" set "IdEmisor" = 'ESSAL' WHERE "IdEmisor" = '' and "IdTipoInstrumento" IN('ACN') AND "RazonSocial" like '%DE LOS LAGOS%'
update "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" set "IdEmisor" = 'POTASIOS' WHERE "IdEmisor" = '' and "IdTipoInstrumento" IN('ACN') AND  "RazonSocial" like '%POTA%'
update "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" set "IdEmisor" = 'ESVAL' WHERE "IdEmisor" = '' and "IdTipoInstrumento" IN('ACN') AND "Nemotecnico" like '%ESVAL%'
update "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" set "IdEmisor" = 'CALICHERA' WHERE "IdEmisor" = '' and "IdTipoInstrumento" IN('ACN') AND  "RazonSocial" like '%CALICHERA%'
update "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" set "IdEmisor" = 'CDCRAIG' WHERE "IdEmisor" = '' and "IdTipoInstrumento" IN('ACN') AND "Nemotecnico" like '%CDCRA%'
update "BSCL"."BSCL.Plataforma.BD::INS.Instrumento" set "IdEmisor" = 'CHILE' WHERE "IdEmisor" = '' and "IdTipoInstrumento" IN('ACN') AND "Nemotecnico" like '%CHILE%'

------------------------------------------------------------------------
Corregir COD_SVS

update bscl."BSCL.Mercado.BD::MER.Emisor"  
  set  "CodigoSVS" = utemis."COD_SVS", "RazonSocial" = utemis."RAZON_SOCIAL"
 from bscl."BSCL.Mercado.BD::MER.Emisor"  as EMI 
	inner join "BSCL_VIRTUAL"."RUE_TBUTEMIS" as utemis on ltrim(rtrim(utemis."EMISOR")) = ltrim(rtrim(EMI."IdEmisor"))
where bscl."BSCL.Mercado.BD::MER.Emisor"."IdEmisor" = EMI."IdEmisor"
  	

