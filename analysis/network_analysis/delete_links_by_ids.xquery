copy $net := doc("original_network.xml")
modify(
let $ids_to_delete_doc := doc("link_ids.xml")
let $ids_to_delete := fn:tokenize($ids_to_delete_doc, "\n")
for $l in $net/network/links/link
where $l/@id = $ids_to_delete
return delete node $l
)
return $net