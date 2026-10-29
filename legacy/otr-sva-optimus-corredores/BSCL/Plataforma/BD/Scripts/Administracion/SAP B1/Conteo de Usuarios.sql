select USER_CODE ,U_NAME,GROUPS   from ousr where 
	user_code not like '%manag%' and 
	user_code not like '%bscl%' and 
	user_code not like '%scl%' and 
	user_code not like '%BSCL%' and 
	user_code not like '%sapserv%' and 
	user_code not like '%gfigueroa%' and 
	"lastLogin" >= (:AGNO || '-' || :MES || '-01')
order by "lastLogin"

--SCOTIA
ffuentes	ffuentes
ocisternc	ocisternc
cbenaven	cbenaven

--TANNER
bsalazar	bsalazar
currea	Claudia Alejandra Urrea Palma
jbascuñan	Jeremy Bascuñan
jalamos	Jacqueline Paz Alamos Gonzalez
cpoblete	Cristian Poblete

--SURA
lmachuca	lmachuca
gmunoz	gmunoz

--NEVASA
cvalenzuela	Claudio Valenzuela
framirez	Francisco Ramirez
dgormaz	Deyanira Gormaz
adiaz	Angelo Díaz
csalinas	Carolina Salinas
pcubillos	Palmenia Cubillos

--MBI
cespinoz	Cynthia Espinoza
pmoyak	Pablo Moya
lortiz	Luis Ortiz Diaz

--EUROAMERICA
aacevedo	Andre Acevedo
bancacoy	Bianca Ancacoy
krivera	Karen Rivera
faflores	Fabiola Flores
kbrockwa	Katherin Brockway

--CREDICORP IMTSECUR
froco	froco
lsanabri	lsanabri
ariquelm	ariquelm
Fmillall	Fredy Millallanca

--CREDICORP IMTPERU

--CREDICORP IMTINV
ariquelm	ariquelm
rloayza	Rubén Loayza Ortiz

--CREDICORP IMTINT
ariquelm	ariquelm
lperalta	Leila Peralta
crojas	Carol Rojas Malverdi

--CREDICORP IMTHOLD
mflorez	mflorez
ycardozo	ycardozo
crojas	Carol Rojas Malverdi
morellan	Macarena Orellana Barahona
sonettoh	sonettoh
maguilar	maguilar
solivare	Sebastian Olivares Gonzalez
lperalta	Leila Peralta
jramirez	jramirez
lsanabri	Leidy Sanabri
lsantos	Lina Santos
jmolina	Johana Molina
iricuart	Ingrid Ricuarte
aleyton	aleyton
ariquelm	ariquelm
cvazques	cvazques
fhermosi	Fabiola Hermosilla
nmarin	Natalie Marin Rios

--CREDICORP IMTCORR
mflorez	mflorez
fhermosi	Fabiola Hermosilla
jpuentes	jpuentes
ycardozo	ycardozo
crojas	Carol Rojas Malverdi
sonettoh	sonettoh
maguilar	maguilar
lsanabri	Leidy Sanabria
jmolina	jmolina
morellan	Macarena Orellana Barahona
jramirez	jramirez
pnieto	pnieto
ariquelm	ariquelm
rgarcia	rgarcia
lperalta	Leila Peralta
lsantos	lsantos
cvazques	cvazques
iricuart	Ingris Ricuarte
froco	froco

--CREDICORP IMTASFIN
solivare	Sebastian Olivares Gonzalez
ycardozo	ycardozo
mflorez	mflorez
crojas	Carol Rojas Malverdi
sonettoh	sonettoh
ariquelm	ariquelm
morellan	Macarena Orellana Barahona
maguilar	maguilar
lsanabri	lsanabri
lperalta	Leila Peralta
lsantos	lsantos
jmolina	jmolina
fhermosi	Fabiola Hermosilla
iricuart	Ingrid Rucarte

--CREDICORP IMTADMINV
cvazques	cvazques
avargas	avargas
mflorez	mflorez
wrojas	wrojas
crojas	Carol Rojas Malverdi
morellan	Macarena Orellana Barahona
sonettoh	sonettoh
ariquelm	ariquelm
maguilar	maguilar
lperalta	Leila Peralta
lsanabri	lsanabri
jramirez	jramirez
fhermosi	Fabiola Hermosilla
lsantos	lsantos
jmolina	jmolina
iricuart	Ingrid Ricuarte
solivare	Sebastian Olivares Gonzalez

--CREDICORP FIPB
ariquelm	ariquelm

--CREDICORP BCP
crojas	Carol Rojas Malverdi
lsanabri	lsanabri
morellan	Macarena Orellana Barahona
fhermosi	Fabiola Hermosilla
lperalta	Leila Peralta

--CREDICORP AGF
ymoncada	Yasna Moncada
mflorez	mflorez
jramirez	jramirez
ycardozo	Yuri Cardozo
crojas	Carol Rojas Malverdi
sonettoh	sonettoh
ariquelm	ariquelm
aleyton	aleyton
maguilar	Michael Aguilar
fhermosi	Fabiola Hermosilla
morellan	Macarena Orellana Barahona
lperalta	Leila Peralta
avargas	Andres Vargas
lsanabri	Leidy Sanabria
iricuart	Ingrid Ricaute
jmolina	Johana Molina
lsantos	Lina Santos
solivare	Sebastian Olivares Gonzalez
abobadil	Arnaldo Bobadilla


SELECT * FROM 


SELECT 'SBOAGF' AS COMPANY,USr.USERID ,usr.u_name,STRING_AGG(GRP."GroupName",',') as GRUPOS,"lastLogin" 
FROM SBOAGF.ousr USr
LEFT JOIN SBOAGF.usr7 usergroup ON usr.userid = usergroup."UserId"
LEFT JOIN SBOAGF.ougr grp ON grp."GroupId" = USERGROUP."GroupId"
GROUP BY USR.USERID,USR.U_NAME,"lastLogin" 
UNION
SELECT 'SBOBCP' AS COMPANY,USr.USERID ,usr.u_name,STRING_AGG(GRP."GroupName",',') as GRUPOS,"lastLogin" 
FROM SBOBCP.ousr USr
LEFT JOIN SBOBCP.usr7 usergroup ON usr.userid = usergroup."UserId"
LEFT JOIN SBOBCP.ougr grp ON grp."GroupId" = USERGROUP."GroupId"
GROUP BY USR.USERID,USR.U_NAME,"lastLogin" 
UNION ALL
SELECT 'SBOFIPB' AS COMPANY,USr.USERID ,usr.u_name,STRING_AGG(GRP."GroupName",',') as GRUPOS,"lastLogin" 
FROM SBOFIPB.ousr USr
LEFT JOIN SBOFIPB.usr7 usergroup ON usr.userid = usergroup."UserId"
LEFT JOIN SBOFIPB.ougr grp ON grp."GroupId" = USERGROUP."GroupId"
GROUP BY USR.USERID,USR.U_NAME,"lastLogin" 
UNION ALL
SELECT 'SBOIMTADMINV' AS COMPANY,USr.USERID ,usr.u_name,STRING_AGG(GRP."GroupName",',') as GRUPOS,"lastLogin" 
FROM SBOIMTADMINV.ousr USr
LEFT JOIN SBOIMTADMINV.usr7 usergroup ON usr.userid = usergroup."UserId"
LEFT JOIN SBOIMTADMINV.ougr grp ON grp."GroupId" = USERGROUP."GroupId"
GROUP BY USR.USERID,USR.U_NAME,"lastLogin" 
UNION ALL
SELECT 'SBOIMTASFIN' AS COMPANY,USr.USERID ,usr.u_name,STRING_AGG(GRP."GroupName",',') as GRUPOS,"lastLogin" 
FROM SBOIMTASFIN.ousr USr
LEFT JOIN SBOIMTASFIN.usr7 usergroup ON usr.userid = usergroup."UserId"
LEFT JOIN SBOIMTASFIN.ougr grp ON grp."GroupId" = USERGROUP."GroupId"
GROUP BY USR.USERID,USR.U_NAME,"lastLogin" 
UNION ALL
SELECT 'SBOIMTCORR' AS COMPANY,USr.USERID ,usr.u_name,STRING_AGG(GRP."GroupName",',') as GRUPOS,"lastLogin" 
FROM SBOIMTCORR.ousr USr
LEFT JOIN SBOIMTCORR.usr7 usergroup ON usr.userid = usergroup."UserId"
LEFT JOIN SBOIMTCORR.ougr grp ON grp."GroupId" = USERGROUP."GroupId"
GROUP BY USR.USERID,USR.U_NAME,"lastLogin" 
UNION ALL
SELECT 'SBOIMTHOLD' AS COMPANY,USr.USERID ,usr.u_name,STRING_AGG(GRP."GroupName",',') as GRUPOS,"lastLogin" 
FROM SBOIMTHOLD.ousr USr
LEFT JOIN SBOIMTHOLD.usr7 usergroup ON usr.userid = usergroup."UserId"
LEFT JOIN SBOIMTHOLD.ougr grp ON grp."GroupId" = USERGROUP."GroupId"
GROUP BY USR.USERID,USR.U_NAME,"lastLogin" 
UNION ALL
SELECT 'SBOIMTINT' AS COMPANY,USr.USERID ,usr.u_name,STRING_AGG(GRP."GroupName",',') as GRUPOS,"lastLogin" 
FROM SBOIMTINT.ousr USr
LEFT JOIN SBOIMTINT.usr7 usergroup ON usr.userid = usergroup."UserId"
LEFT JOIN SBOIMTINT.ougr grp ON grp."GroupId" = USERGROUP."GroupId"
GROUP BY USR.USERID,USR.U_NAME,"lastLogin" 
UNION ALL
SELECT 'SBOIMTINV' AS COMPANY,USr.USERID ,usr.u_name,STRING_AGG(GRP."GroupName",',') as GRUPOS,"lastLogin" 
FROM SBOIMTINV.ousr USr
LEFT JOIN SBOIMTINV.usr7 usergroup ON usr.userid = usergroup."UserId"
LEFT JOIN SBOIMTINV.ougr grp ON grp."GroupId" = USERGROUP."GroupId"
GROUP BY USR.USERID,USR.U_NAME,"lastLogin" 
UNION ALL
SELECT 'SBOIMTPERU' AS COMPANY,USr.USERID ,usr.u_name,STRING_AGG(GRP."GroupName",',') as GRUPOS,"lastLogin" 
FROM SBOIMTPERU.ousr USr
LEFT JOIN SBOIMTPERU.usr7 usergroup ON usr.userid = usergroup."UserId"
LEFT JOIN SBOIMTPERU.ougr grp ON grp."GroupId" = USERGROUP."GroupId"
GROUP BY USR.USERID,USR.U_NAME,"lastLogin" 
UNION ALL
SELECT 'SBOIMTSECUR' AS COMPANY,USr.USERID ,usr.u_name,STRING_AGG(GRP."GroupName",',') as GRUPOS,"lastLogin" 
FROM SBOIMTSECUR.ousr USr
LEFT JOIN SBOIMTSECUR.usr7 usergroup ON usr.userid = usergroup."UserId"
LEFT JOIN SBOIMTSECUR.ougr grp ON grp."GroupId" = USERGROUP."GroupId"
GROUP BY USR.USERID,USR.U_NAME,"lastLogin" 
UNION ALL
SELECT 'SAPMARCHABLANCA' AS COMPANY,USr.USERID ,usr.u_name,STRING_AGG(GRP."GroupName",',') as GRUPOS,"lastLogin" 
FROM SAPMARCHABLANCA.ousr USr
LEFT JOIN SAPMARCHABLANCA.usr7 usergroup ON usr.userid = usergroup."UserId"
LEFT JOIN SAPMARCHABLANCA.ougr grp ON grp."GroupId" = USERGROUP."GroupId"
GROUP BY USR.USERID,USR.U_NAME,"lastLogin" 