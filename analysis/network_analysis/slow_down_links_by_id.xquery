copy $net := doc("large_original_network.xml")
modify(
let $ids_to_delete_doc := doc("link_ids.xml")
let $ids_to_delete := fn:tokenize($ids_to_delete_doc, "\n")
for $l in $net/network/links/link
where $l/@id = $ids_to_delete
return (
replace value of node $l/@freespeed with '1.388888888888888',
replace value of node $l/@capacity with '10000.0'
)
)
return $net