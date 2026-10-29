set schema BSCL;
 
--este script revisa las principales organizaciones del mercado chile 
--DCV,CCLV,BOLSA DE DANTIAGO, ELECTRONICA,VALPARAISO 
-- el cliente para que pueda ser contraparte debe contar con los datos basicos
--correo electronico
--direccion
-- telefono
-- si esa informacion al momento de entrar al mantenedor de contraparte no se puede crear
-- debido esto si el cliente viene por migracion
-- no se puede crear la contraparte

--se revisa y se crean las contraparte tambien se actualiza la tabla conceptoscustodia 
--para los conceptos de garantia  ya que sin eso no es posible realizar el ingreso de garantia
 
 select "DocumentoIdentidad",* from "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad"
 where "DocumentoIdentidad"  in ( 
 '96666140-2'
,'90249000-0'
,'96518240-3'
,'96551730-8'
,'96572920-8'
,'80993900-6')
  
select * 
from "BSCL.SocioNegocio.BD::PER.Persona" AS A1
,"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as A2
WHERE A1."IdDocumentoIdentidad" = A2."IdDocumentoIdentidad"
AND A2."DocumentoIdentidad" in ( 
 '96666140-2'
,'90249000-0'
,'96518240-3'
,'96551730-8'
,'96572920-8'
,'80993900-6')


UPDATE A1 SET A1."IdDireccionElectronicaPrincipal" = A2."IdDireccionElectronica"
FROM
"BSCL.SocioNegocio.BD::PER.Persona" AS A1
,(select A1."Direccion",A3."IdPersona","IdDireccionElectronica" 
from "BSCL.SocioNegocio.BD::PER.DireccionElectronica" AS A1
,"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as A2
,"BSCL.SocioNegocio.BD::PER.Persona" as A3
WHERE A2."IdDocumentoIdentidad" = A3."IdDocumentoIdentidad"
AND   A3."IdPersona" = A1."IdPersona"
AND A2."DocumentoIdentidad" in ( 
 '96666140-2'
,'90249000-0'
,'96518240-3'
,'96551730-8'
,'96572920-8'
,'80993900-6')) AS A2
WHERE A1."IdPersona" = A2."IdPersona"

-- en caso que le falte una direccion electronica la podemos crear asi 
--revisamos si la persona tiene una direccion fisica

select * from "BSCL.SocioNegocio.BD::PER.DireccionElectronica" where "IdPersona" = '380997'
select top 1 * from "BSCL.SocioNegocio.BD::PER.DireccionElectronica"

-- en caso que no la tenga la ingresaremos 
insert into "BSCL.SocioNegocio.BD::PER.DireccionElectronica"
select "BSCL.SocioNegocio.BD.Secuencias::DireccionElectronica".NEXTVAL
,'bcs@bolsadesantiago.com'
,''
,''
,'ACT'--ESTADO
,'0001-01-01'
,'0001-01-01'
,'0001-01-01'
,380997--ID PERSONA
,'ING'
,88 --ID CARGA
,90249000--ORIGEN RUT SIN DIGITO VERIFICADOR
,'N'-- ESTA BORRADO
,0--
from dummy;

-- revisamos si tiene direccion fisica y tiene asignada una comuna 
select * from "BSCL.SocioNegocio.BD::PER.DireccionFisica" where "IdPersona" = '380997'
-- en caso que no la tenga debemos ingresarla  
-- si tiene actualizaremos los camos necesarios en este caso IdDivisionPais, Numero
-- buscamos la comuna por nombre
-- en caso que tenga que actualizarse la comuna de una direccion
select * from "BSCL"."BSCL.Plataforma.BD::REG.DivisionPais" where "NombreDivisionPais" = 'SANTIAGO';
select * from "BSCL"."BSCL.Plataforma.BD::REG.Ciudad" where "NombreCiudad" ='SANTIAGO'

update "BSCL.SocioNegocio.BD::PER.DireccionFisica" set "IdDivisionPais" = 327
													   ,"IdCiudad" =2931
													   ,"Numero" =64			
where "IdDireccionFisica" in (457699,457700); 

-- actualizamos el telefono en caso de error

select * from "BSCL.SocioNegocio.BD::PER.Telefono" where "IdPersona" ='380997'
union all
select top 1 * from "BSCL.SocioNegocio.BD::PER.Telefono" where "IdCarga" = 0;
 
-- actualizaremos el telefono y el tipo de telefono 
update  "BSCL.SocioNegocio.BD::PER.Telefono" set "Numero" = '+59226982001'
                                                ,"CodigoProposito" = 'TTELEFIJO'
                                                ,"IdTipoTelefono" = 'OTRO'
where "IdTelefono" in (337402);

--revisamos si creo la contraparte se creo "se realizo por sistema"
select * from "BSCL.SocioNegocio.BD::CNT.Contraparte" 

UPDATE A1 set A1."RazonSocial" = 'Deposito Central de Valores S.A'
from "BSCL.SocioNegocio.BD::PER.Persona" AS A1
,"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as A2
WHERE A1."IdDocumentoIdentidad" = A2."IdDocumentoIdentidad"
AND A2."DocumentoIdentidad" = '96666140-2';

UPDATE A1 set A1."RazonSocial" = 'Bolsa Comercio Santiago'
from "BSCL.SocioNegocio.BD::PER.Persona" AS A1
,"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as A2
WHERE A1."IdDocumentoIdentidad" = A2."IdDocumentoIdentidad"
AND A2."DocumentoIdentidad" = '90249000-0';

UPDATE A1 set A1."RazonSocial" = 'Bolsa de Comercio de Valparaiso'
from "BSCL.SocioNegocio.BD::PER.Persona" AS A1
,"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as A2
WHERE A1."IdDocumentoIdentidad" = A2."IdDocumentoIdentidad"
AND A2."DocumentoIdentidad" = '96518240-3';

UPDATE A1  set A1."RazonSocial" = 'Bolsa Electronica de Comercio'
from "BSCL.SocioNegocio.BD::PER.Persona" AS A1
,"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as A2
WHERE A1."IdDocumentoIdentidad" = A2."IdDocumentoIdentidad"
AND A2."DocumentoIdentidad" = '96551730-8';

UPDATE A1 set A1."RazonSocial" = 'Contraparte Central S.A'
from "BSCL.SocioNegocio.BD::PER.Persona" AS A1
,"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as A2
WHERE A1."IdDocumentoIdentidad" = A2."IdDocumentoIdentidad"
AND A2."DocumentoIdentidad" = '96572920-8';


-- REVISAMOS LAS CONTRAPARTES
select "RazonSocial",* from "BSCL"."BSCL.SocioNegocio.BD::CNT.Contraparte" as A1
,"BSCL"."BSCL.SocioNegocio.BD::PER.Persona" as A2
WHERE A2."IdPersona" = A1."IdPersona"

-- DEBEMOS ACTULIZAR LAS CONTRAPARTES EN LA TABLAS DE CONCEPTOS 
--DE CUSTODIA

SELECT * FROM "BSCL"."BSCL.Custodia.BD::CUS.ConceptoCustodia"

-- ACTUALIZAMOS LOS CONCEPTOS CUSTODIA
UPDATE A1 SET A1."IdContraparte" = (select A1."IdContraparte"
from "BSCL.SocioNegocio.BD::CNT.Contraparte" AS A1
,"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as A2
,"BSCL.SocioNegocio.BD::PER.Persona" as A3
WHERE A2."IdDocumentoIdentidad" = A3."IdDocumentoIdentidad"
AND   A3."IdPersona" = A1."IdPersona"
AND A2."DocumentoIdentidad" = '90249000-0')--bolsa de comercio de santiago
FROM "BSCL"."BSCL.Custodia.BD::CUS.ConceptoCustodia" AS A1
WHERE A1."NombreEntidad" ='CUS.GARANTIA'
AND A1."IdConcepto" = 'GAINSI';

UPDATE A1 SET A1."IdContraparte" = (select A1."IdContraparte"
from "BSCL.SocioNegocio.BD::CNT.Contraparte" AS A1
,"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as A2
,"BSCL.SocioNegocio.BD::PER.Persona" as A3
WHERE A2."IdDocumentoIdentidad" = A3."IdDocumentoIdentidad"
AND   A3."IdPersona" = A1."IdPersona"
AND A2."DocumentoIdentidad" = '90249000-0')--bolsa de comercio de santiago
FROM "BSCL"."BSCL.Custodia.BD::CUS.ConceptoCustodia" AS A1
WHERE A1."NombreEntidad" ='CUS.GARANTIA'
AND A1."IdConcepto" = 'GAINSP';

UPDATE A1 SET A1."IdContraparte" = (select A1."IdContraparte"
from "BSCL.SocioNegocio.BD::CNT.Contraparte" AS A1
,"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as A2
,"BSCL.SocioNegocio.BD::PER.Persona" as A3
WHERE A2."IdDocumentoIdentidad" = A3."IdDocumentoIdentidad"
AND   A3."IdPersona" = A1."IdPersona"
AND A2."DocumentoIdentidad" = '96666140-2')--DCV
FROM "BSCL"."BSCL.Custodia.BD::CUS.ConceptoCustodia" AS A1
WHERE A1."NombreEntidad" ='CUS.GARANTIA'
AND A1."IdConcepto" = 'GAINFU';


UPDATE A1 SET A1."IdContraparte" = (select A1."IdContraparte"
from "BSCL.SocioNegocio.BD::CNT.Contraparte" AS A1
,"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as A2
,"BSCL.SocioNegocio.BD::PER.Persona" as A3
WHERE A2."IdDocumentoIdentidad" = A3."IdDocumentoIdentidad"
AND   A3."IdPersona" = A1."IdPersona"
AND A2."DocumentoIdentidad" = '90249000-0')--bolsa de comercio de santiago
FROM "BSCL"."BSCL.Custodia.BD::CUS.ConceptoCustodia" AS A1
WHERE A1."NombreEntidad" ='CUS.GARANTIA'
AND A1."IdConcepto" IN ('GAINFW','GAINVC','GAINRF');

UPDATE A8 SET A8."IdContraparteGarantia" = (select A1."IdContraparte"
from "BSCL.SocioNegocio.BD::CNT.Contraparte" AS A1
,"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as A2
,"BSCL.SocioNegocio.BD::PER.Persona" as A3
WHERE A2."IdDocumentoIdentidad" = A3."IdDocumentoIdentidad"
AND   A3."IdPersona" = A1."IdPersona"
AND A2."DocumentoIdentidad" = '90249000-0')
FROM "BSCL"."BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" as A8
WHERE A8."IdTipoInstrumento" not in ('FBC','FDO','FIC','FIP');

UPDATE A8 SET A8."IdContraparteGarantia" = (select A1."IdContraparte"
from "BSCL.SocioNegocio.BD::CNT.Contraparte" AS A1
,"BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as A2
,"BSCL.SocioNegocio.BD::PER.Persona" as A3
WHERE A2."IdDocumentoIdentidad" = A3."IdDocumentoIdentidad"
AND   A3."IdPersona" = A1."IdPersona"
AND A2."DocumentoIdentidad" = '96572920-8')
FROM "BSCL"."BSCL.Plataforma.BD::INS.BolsaTipoInstrumento" as A8
WHERE A8."IdTipoInstrumento" in ('FBC','FDO','FIC','FIP');

select * from "BSCL"."BSCL.Plataforma.BD::INS.BolsaTipoInstrumento"


