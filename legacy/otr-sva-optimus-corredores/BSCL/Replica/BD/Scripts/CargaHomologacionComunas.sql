SET SCHEMA "BSCL";
GO


/*  
SELECT
	DISTINCT 
		'select ' || cast("com_id" AS varchar) || ' AS "ValorOrigen",''COMUNA_SEBRA'' AS "TipoValor",''COMUNA'' AS "Entidad",' ||  CAST ("comId" AS varchar)  || ' AS "ValorDestino",''' || "NombreCiudad" || ''' AS "Descripcion" from dummy union all ' 
	FROM (
		SELECT
			min(DP."IdDivisionPais") AS "comId",-- INT  
			CIUDAD."NombreCiudad"  
		FROM "BSCL.Plataforma.BD::REG.DivisionPais" DP
		JOIN "BSCL.Plataforma.BD::REG.Ciudad" CIUDAD ON CIUDAD."IdPais" = DP."IdPais" AND CIUDAD."IdCiudad" = DP."IdCiudad"
		GROUP BY CIUDAD."NombreCiudad"
	) AS tb JOIN BSCL."BSCL.Homologacion.BD::GES.Comuna"
			ON (upper(trim("com_nombre")) = upper(trim("NombreCiudad"))  )

			
*/


DELETE FROM "BSCL.Homologacion.BD::HOM.Homologacion"	WHERE 	'COMUNA_SEBRA' = "TipoValor"; go
			
			

INSERT INTO "BSCL.Homologacion.BD::HOM.Homologacion"("IdHomologacion","ValorOrigen","TipoValor","Entidad","ValorDestino","Descripcion")
SELECT
    "BSCL.Homologacion.BD.Secuencias::TiposHomologacion".NEXTVAL,
    "ValorOrigen","TipoValor","Entidad","ValorDestino","Descripcion"
FROM (
	
select 5602 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",41 AS "ValorDestino",'Algarrobo' AS "Descripcion" from dummy union all 
select 3302 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",17 AS "ValorDestino",'Alto del Carmen' AS "Descripcion" from dummy union all 
select 1107 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",1 AS "ValorDestino",'Alto Hospicio' AS "Descripcion" from dummy union all 
select 10202 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",228 AS "ValorDestino",'Ancud' AS "Descripcion" from dummy union all 
select 4103 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",26 AS "ValorDestino",'Andacollo' AS "Descripcion" from dummy union all 
select 9201 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",196 AS "ValorDestino",'Angol' AS "Descripcion" from dummy union all 
select 2101 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",8 AS "ValorDestino",'Antofagasta' AS "Descripcion" from dummy union all 
select 8302 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",143 AS "ValorDestino",'Antuco' AS "Descripcion" from dummy union all 
select 8202 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",144 AS "ValorDestino",'Arauco' AS "Descripcion" from dummy union all 
select 15001 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",343 AS "ValorDestino",'Arica' AS "Descripcion" from dummy union all 
select 13402 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",280 AS "ValorDestino",'Buin' AS "Descripcion" from dummy union all 
select 8402 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",145 AS "ValorDestino",'Bulnes' AS "Descripcion" from dummy union all 
select 5402 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",42 AS "ValorDestino",'Cabildo' AS "Descripcion" from dummy union all 
select 8303 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",146 AS "ValorDestino",'Cabrero' AS "Descripcion" from dummy union all 
select 2201 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",9 AS "ValorDestino",'Calama' AS "Descripcion" from dummy union all 
select 10102 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",229 AS "ValorDestino",'Calbuco' AS "Descripcion" from dummy union all 
select 3102 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",18 AS "ValorDestino",'Caldera' AS "Descripcion" from dummy union all 
select 13403 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",281 AS "ValorDestino",'Calera de Tango' AS "Descripcion" from dummy union all 
select 5302 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",44 AS "ValorDestino",'Calle Larga' AS "Descripcion" from dummy union all 
select 1102 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",2 AS "ValorDestino",'Camiña' AS "Descripcion" from dummy union all 
select 8203 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",147 AS "ValorDestino",'Cañete' AS "Descripcion" from dummy union all 
select 9102 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",197 AS "ValorDestino",'Carahue' AS "Descripcion" from dummy union all 
select 5603 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",45 AS "ValorDestino",'Cartagena' AS "Descripcion" from dummy union all 
select 27347 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",45 AS "ValorDestino",'Cartagena' AS "Descripcion" from dummy union all 
select 26672 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",46 AS "ValorDestino",'Casablanca' AS "Descripcion" from dummy union all 
select 5102 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",46 AS "ValorDestino",'Casablanca' AS "Descripcion" from dummy union all 
select 10201 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",230 AS "ValorDestino",'Castro' AS "Descripcion" from dummy union all 
select 5702 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",47 AS "ValorDestino",'Catemu' AS "Descripcion" from dummy union all 
select 7201 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",112 AS "ValorDestino",'Cauquenes' AS "Descripcion" from dummy union all 
select 10401 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",231 AS "ValorDestino",'Chaitén' AS "Descripcion" from dummy union all 
select 3201 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",19 AS "ValorDestino",'Chañaral' AS "Descripcion" from dummy union all 
select 7202 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",113 AS "ValorDestino",'Chanco' AS "Descripcion" from dummy union all 
select 6302 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",79 AS "ValorDestino",'Chépica' AS "Descripcion" from dummy union all 
select 8103 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",148 AS "ValorDestino",'Chiguayante' AS "Descripcion" from dummy union all 
select 11401 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",259 AS "ValorDestino",'Chile Chico' AS "Descripcion" from dummy union all 
select 8401 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",149 AS "ValorDestino",'Chillán' AS "Descripcion" from dummy union all 
select 6303 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",80 AS "ValorDestino",'Chimbarongo' AS "Descripcion" from dummy union all 
select 9121 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",198 AS "ValorDestino",'Cholchol' AS "Descripcion" from dummy union all 
select 10203 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",232 AS "ValorDestino",'Chonchi' AS "Descripcion" from dummy union all 
select 8403 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",151 AS "ValorDestino",'Cobquecura' AS "Descripcion" from dummy union all 
select 11301 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",261 AS "ValorDestino",'Cochrane' AS "Descripcion" from dummy union all 
select 6102 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",81 AS "ValorDestino",'Codegua' AS "Descripcion" from dummy union all 
select 8404 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",152 AS "ValorDestino",'Coelemu' AS "Descripcion" from dummy union all 
select 11101 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",262 AS "ValorDestino",'Coihaique' AS "Descripcion" from dummy union all 
select 8405 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",153 AS "ValorDestino",'Coihueco' AS "Descripcion" from dummy union all 
select 6103 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",82 AS "ValorDestino",'Coinco' AS "Descripcion" from dummy union all 
select 7402 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",114 AS "ValorDestino",'Colbún' AS "Descripcion" from dummy union all 
select 1103 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",3 AS "ValorDestino",'Colchane ' AS "Descripcion" from dummy union all 
select 13301 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",284 AS "ValorDestino",'Colina' AS "Descripcion" from dummy union all 
select 9202 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",199 AS "ValorDestino",'Collipulli' AS "Descripcion" from dummy union all 
select 6104 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",83 AS "ValorDestino",'Coltauco' AS "Descripcion" from dummy union all 
select 4302 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",28 AS "ValorDestino",'Combarbalá' AS "Descripcion" from dummy union all 
select 8101 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",154 AS "ValorDestino",'Concepción' AS "Descripcion" from dummy union all 
select 5103 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",48 AS "ValorDestino",'Concón' AS "Descripcion" from dummy union all 
select 7102 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",115 AS "ValorDestino",'Constitución' AS "Descripcion" from dummy union all 
select 8204 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",155 AS "ValorDestino",'Contulmo' AS "Descripcion" from dummy union all 
select 3101 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",20 AS "ValorDestino",'Copiapó' AS "Descripcion" from dummy union all 
select 4102 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",29 AS "ValorDestino",'Coquimbo' AS "Descripcion" from dummy union all 
select 8102 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",156 AS "ValorDestino",'Coronel' AS "Descripcion" from dummy union all 
select 14102 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",331 AS "ValorDestino",'Corral' AS "Descripcion" from dummy union all 
select 9103 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",200 AS "ValorDestino",'Cunco' AS "Descripcion" from dummy union all 
select 9203 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",201 AS "ValorDestino",'Curacautín' AS "Descripcion" from dummy union all 
select 13503 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",286 AS "ValorDestino",'Curacaví' AS "Descripcion" from dummy union all 
select 10204 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",234 AS "ValorDestino",'Curaco de Vélez' AS "Descripcion" from dummy union all 
select 8205 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",157 AS "ValorDestino",'Curanilahue' AS "Descripcion" from dummy union all 
select 9104 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",202 AS "ValorDestino",'Curarrehue' AS "Descripcion" from dummy union all 
select 7103 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",116 AS "ValorDestino",'Curepto' AS "Descripcion" from dummy union all 
select 7301 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",117 AS "ValorDestino",'Curicó' AS "Descripcion" from dummy union all 
select 10205 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",235 AS "ValorDestino",'Dalcahue' AS "Descripcion" from dummy union all 
select 3202 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",21 AS "ValorDestino",'Diego de Almagro' AS "Descripcion" from dummy union all 
select 6105 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",84 AS "ValorDestino",'Doñihue' AS "Descripcion" from dummy union all 
select 8407 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",158 AS "ValorDestino",'El Carmen' AS "Descripcion" from dummy union all 
select 13602 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",288 AS "ValorDestino",'El Monte' AS "Descripcion" from dummy union all 
select 5604 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",49 AS "ValorDestino",'El Quisco' AS "Descripcion" from dummy union all 
select 5605 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",50 AS "ValorDestino",'El Tabo' AS "Descripcion" from dummy union all 
select 7104 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",118 AS "ValorDestino",'Empedrado' AS "Descripcion" from dummy union all 
select 9204 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",203 AS "ValorDestino",'Ercilla' AS "Descripcion" from dummy union all 
select 8104 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",159 AS "ValorDestino",'Florida' AS "Descripcion" from dummy union all 
select 9105 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",204 AS "ValorDestino",'Freire' AS "Descripcion" from dummy union all 
select 3303 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",22 AS "ValorDestino",'Freirina' AS "Descripcion" from dummy union all 
select 10104 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",236 AS "ValorDestino",'Fresia' AS "Descripcion" from dummy union all 
select 10105 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",237 AS "ValorDestino",'Frutillar' AS "Descripcion" from dummy union all 
select 10402 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",238 AS "ValorDestino",'Futaleufú' AS "Descripcion" from dummy union all 
select 14202 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",332 AS "ValorDestino",'Futrono' AS "Descripcion" from dummy union all 
select 9106 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",205 AS "ValorDestino",'Galvarino' AS "Descripcion" from dummy union all 
select 9107 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",206 AS "ValorDestino",'Gorbea' AS "Descripcion" from dummy union all 
select 6106 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",85 AS "ValorDestino",'Graneros' AS "Descripcion" from dummy union all 
select 5503 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",51 AS "ValorDestino",'Hijuelas' AS "Descripcion" from dummy union all 
select 7302 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",119 AS "ValorDestino",'Hualañé' AS "Descripcion" from dummy union all 
select 8105 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",161 AS "ValorDestino",'Hualqui' AS "Descripcion" from dummy union all 
select 1104 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",4 AS "ValorDestino",'Huara' AS "Descripcion" from dummy union all 
select 3304 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",23 AS "ValorDestino",'Huasco' AS "Descripcion" from dummy union all 
select 4201 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",30 AS "ValorDestino",'Illapel' AS "Descripcion" from dummy union all 
select 1101 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",5 AS "ValorDestino",'Iquique' AS "Descripcion" from dummy union all 
select 13603 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",292 AS "ValorDestino",'Isla de Maipo' AS "Descripcion" from dummy union all 
select 5504 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",54 AS "ValorDestino",'La Cruz' AS "Descripcion" from dummy union all 
select 6202 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",86 AS "ValorDestino",'La Estrella ' AS "Descripcion" from dummy union all 
select 4104 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",31 AS "ValorDestino",'La Higuera' AS "Descripcion" from dummy union all 
select 5401 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",55 AS "ValorDestino",'La Ligua' AS "Descripcion" from dummy union all 
select 4101 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",32 AS "ValorDestino",'La Serena' AS "Descripcion" from dummy union all 
select 14201 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",333 AS "ValorDestino",'La Unión' AS "Descripcion" from dummy union all 
select 14203 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",334 AS "ValorDestino",'Lago Ranco' AS "Descripcion" from dummy union all 
select 11102 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",264 AS "ValorDestino",'Lago Verde' AS "Descripcion" from dummy union all 
select 8304 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",162 AS "ValorDestino",'Laja' AS "Descripcion" from dummy union all 
select 13302 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",298 AS "ValorDestino",'Lampa' AS "Descripcion" from dummy union all 
select 14103 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",335 AS "ValorDestino",'Lanco' AS "Descripcion" from dummy union all 
select 6107 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",87 AS "ValorDestino",'Las Cabras' AS "Descripcion" from dummy union all 
select 9108 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",207 AS "ValorDestino",'Lautaro' AS "Descripcion" from dummy union all 
select 8201 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",163 AS "ValorDestino",'Lebu' AS "Descripcion" from dummy union all 
select 7303 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",120 AS "ValorDestino",'Licantén' AS "Descripcion" from dummy union all 
select 5505 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",56 AS "ValorDestino",'Limache' AS "Descripcion" from dummy union all 
select 7401 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",121 AS "ValorDestino",'Linares' AS "Descripcion" from dummy union all 
select 6203 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",88 AS "ValorDestino",'Litueche' AS "Descripcion" from dummy union all 
select 10107 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",240 AS "ValorDestino",'Llanquihue' AS "Descripcion" from dummy union all 
select 6304 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",89 AS "ValorDestino",'Lolol' AS "Descripcion" from dummy union all 
select 9109 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",208 AS "ValorDestino",'Loncoche' AS "Descripcion" from dummy union all 
select 7403 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",122 AS "ValorDestino",'Longaví' AS "Descripcion" from dummy union all 
select 9205 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",209 AS "ValorDestino",'Lonquimay' AS "Descripcion" from dummy union all 
select 8206 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",164 AS "ValorDestino",'Los Alamos' AS "Descripcion" from dummy union all 
select 5301 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",58 AS "ValorDestino",'Los Andes' AS "Descripcion" from dummy union all 
select 8301 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",165 AS "ValorDestino",'Los Angeles' AS "Descripcion" from dummy union all 
select 14104 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",336 AS "ValorDestino",'Los Lagos' AS "Descripcion" from dummy union all 
select 10106 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",241 AS "ValorDestino",'Los Muermos' AS "Descripcion" from dummy union all 
select 9206 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",210 AS "ValorDestino",'Los Sauces' AS "Descripcion" from dummy union all 
select 4203 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",33 AS "ValorDestino",'Los Vilos' AS "Descripcion" from dummy union all 
select 8106 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",166 AS "ValorDestino",'Lota' AS "Descripcion" from dummy union all 
select 6108 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",90 AS "ValorDestino",'Machalí' AS "Descripcion" from dummy union all 
select 14105 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",337 AS "ValorDestino",'Máfil' AS "Descripcion" from dummy union all 
select 6109 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",91 AS "ValorDestino",'Malloa' AS "Descripcion" from dummy union all 
select 6204 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",92 AS "ValorDestino",'Marchihue' AS "Descripcion" from dummy union all 
select 2302 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",10 AS "ValorDestino",'María Elena' AS "Descripcion" from dummy union all 
select 13504 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",305 AS "ValorDestino",'María Pinto ' AS "Descripcion" from dummy union all 
select 7105 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",123 AS "ValorDestino",'Maule' AS "Descripcion" from dummy union all 
select 10108 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",242 AS "ValorDestino",'Maullín' AS "Descripcion" from dummy union all 
select 2102 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",11 AS "ValorDestino",'Mejillones' AS "Descripcion" from dummy union all 
select 9110 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",212 AS "ValorDestino",'Melipeuco' AS "Descripcion" from dummy union all 
select 13501 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",306 AS "ValorDestino",'Melipilla' AS "Descripcion" from dummy union all 
select 7304 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",124 AS "ValorDestino",'Molina' AS "Descripcion" from dummy union all 
select 4303 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",34 AS "ValorDestino",'Monte Patria' AS "Descripcion" from dummy union all 
select 8305 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",167 AS "ValorDestino",'Mulchén' AS "Descripcion" from dummy union all 
select 8306 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",168 AS "ValorDestino",'Nacimiento' AS "Descripcion" from dummy union all 
select 6305 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",94 AS "ValorDestino",'Nancagua' AS "Descripcion" from dummy union all 
select 6205 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",95 AS "ValorDestino",'Navidad    ' AS "Descripcion" from dummy union all 
select 8307 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",169 AS "ValorDestino",'Negrete' AS "Descripcion" from dummy union all 
select 8408 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",170 AS "ValorDestino",'Ninhue' AS "Descripcion" from dummy union all 
select 5506 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",59 AS "ValorDestino",'Nogales' AS "Descripcion" from dummy union all 
select 26642 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",59 AS "ValorDestino",'Nogales' AS "Descripcion" from dummy union all 
select 9111 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",213 AS "ValorDestino",'Nueva Imperial' AS "Descripcion" from dummy union all 
select 2202 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",12 AS "ValorDestino",'Ollagüe' AS "Descripcion" from dummy union all 
select 5507 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",60 AS "ValorDestino",'Olmué' AS "Descripcion" from dummy union all 
select 10301 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",243 AS "ValorDestino",'Osorno' AS "Descripcion" from dummy union all 
select 4301 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",35 AS "ValorDestino",'Ovalle' AS "Descripcion" from dummy union all 
select 13604 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",308 AS "ValorDestino",'Padre Hurtado' AS "Descripcion" from dummy union all 
select 4105 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",36 AS "ValorDestino",'Paiguano' AS "Descripcion" from dummy union all 
select 14107 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",339 AS "ValorDestino",'Paillaco' AS "Descripcion" from dummy union all 
select 13404 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",309 AS "ValorDestino",'Paine' AS "Descripcion" from dummy union all 
select 10404 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",244 AS "ValorDestino",'Palena' AS "Descripcion" from dummy union all 
select 6306 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",97 AS "ValorDestino",'Palmilla' AS "Descripcion" from dummy union all 
select 14108 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",340 AS "ValorDestino",'Panguipulli' AS "Descripcion" from dummy union all 
select 5704 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",61 AS "ValorDestino",'Panquehue' AS "Descripcion" from dummy union all 
select 5403 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",62 AS "ValorDestino",'Papudo' AS "Descripcion" from dummy union all 
select 6206 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",98 AS "ValorDestino",'Paredones' AS "Descripcion" from dummy union all 
select 7404 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",125 AS "ValorDestino",'Parral' AS "Descripcion" from dummy union all 
select 7106 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",126 AS "ValorDestino",'Pelarco' AS "Descripcion" from dummy union all 
select 8410 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",172 AS "ValorDestino",'Pemuco' AS "Descripcion" from dummy union all 
select 13605 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",311 AS "ValorDestino",'Peñaflor' AS "Descripcion" from dummy union all 
select 7107 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",128 AS "ValorDestino",'Pencahue' AS "Descripcion" from dummy union all 
select 8107 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",173 AS "ValorDestino",'Penco' AS "Descripcion" from dummy union all 
select 6307 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",99 AS "ValorDestino",'Peralillo' AS "Descripcion" from dummy union all 
select 9113 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",215 AS "ValorDestino",'Perquenco' AS "Descripcion" from dummy union all 
select 5404 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",63 AS "ValorDestino",'Petorca   ' AS "Descripcion" from dummy union all 
select 6112 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",100 AS "ValorDestino",'Peumo' AS "Descripcion" from dummy union all 
select 1105 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",6 AS "ValorDestino",'Pica' AS "Descripcion" from dummy union all 
select 6113 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",101 AS "ValorDestino",'Pichidegua' AS "Descripcion" from dummy union all 
select 6201 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",102 AS "ValorDestino",'Pichilemu' AS "Descripcion" from dummy union all 
select 8411 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",174 AS "ValorDestino",'Pinto' AS "Descripcion" from dummy union all 
select 9114 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",216 AS "ValorDestino",'Pitrufquén' AS "Descripcion" from dummy union all 
select 6308 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",103 AS "ValorDestino",'Placilla' AS "Descripcion" from dummy union all 
select 8412 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",175 AS "ValorDestino",'Portezuelo' AS "Descripcion" from dummy union all 
select 1106 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",7 AS "ValorDestino",'Pozo Almonte' AS "Descripcion" from dummy union all 
select 5105 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",64 AS "ValorDestino",'Puchuncaví' AS "Descripcion" from dummy union all 
select 9115 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",217 AS "ValorDestino",'Pucón' AS "Descripcion" from dummy union all 
select 10101 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",245 AS "ValorDestino",'Puerto Montt' AS "Descripcion" from dummy union all 
select 10302 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",246 AS "ValorDestino",'Puerto Octay' AS "Descripcion" from dummy union all 
select 10109 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",247 AS "ValorDestino",'Puerto Varas' AS "Descripcion" from dummy union all 
select 6309 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",104 AS "ValorDestino",'Pumanque' AS "Descripcion" from dummy union all 
select 4304 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",37 AS "ValorDestino",'Punitaqui' AS "Descripcion" from dummy union all 
select 12101 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",274 AS "ValorDestino",'Punta Arenas' AS "Descripcion" from dummy union all 
select 10206 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",248 AS "ValorDestino",'Puqueldón' AS "Descripcion" from dummy union all 
select 9208 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",218 AS "ValorDestino",'Purén' AS "Descripcion" from dummy union all 
select 10303 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",249 AS "ValorDestino",'Purranque' AS "Descripcion" from dummy union all 
select 5705 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",65 AS "ValorDestino",'Putaendo' AS "Descripcion" from dummy union all 
select 15201 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",346 AS "ValorDestino",'Putre' AS "Descripcion" from dummy union all 
select 10207 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",251 AS "ValorDestino",'Queilén' AS "Descripcion" from dummy union all 
select 10208 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",252 AS "ValorDestino",'Quellón' AS "Descripcion" from dummy union all 
select 10209 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",253 AS "ValorDestino",'Quemchi' AS "Descripcion" from dummy union all 
select 8308 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",176 AS "ValorDestino",'Quilaco' AS "Descripcion" from dummy union all 
select 8309 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",177 AS "ValorDestino",'Quilleco' AS "Descripcion" from dummy union all 
select 8413 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",178 AS "ValorDestino",'Quillón' AS "Descripcion" from dummy union all 
select 5501 AS "ValorOrigen",'COMUNA_SEBRA' AS "TipoValor",'COMUNA' AS "Entidad",66 AS "ValorDestino",'Quillota' AS "Descripcion" from dummy 

    ) AS t
			
    
  