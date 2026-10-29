delete from public."SessionActivity" where "sessionId" in (select id from public."Session" where "brokerId" in (5,4) )
delete from public."Session" where "brokerId" in (5,4) 