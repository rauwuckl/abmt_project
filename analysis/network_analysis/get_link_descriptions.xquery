let $raw_ids_doc := doc("link_ids.xml")
let $raw_ids := fn:tokenize($raw_ids_doc/link_ids, "\n")
for $id in $raw_ids
where $id
return <link_id> {$id} </link_id>