export function usePageSeo(titleKey: string, descriptionKey: string) {
  const {t} = useI18n()
  const title = t(titleKey)
  const description = t(descriptionKey)

  useHead({title})
  useSeoMeta({
    description,
    ogTitle: title,
    ogDescription: description,
    twitterTitle: title,
    twitterDescription: description,
  })
}
