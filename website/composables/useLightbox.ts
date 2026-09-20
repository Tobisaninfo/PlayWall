const state = reactive<{ src: string | null, alt: string, width: number, height: number }>({
    src: null,
    alt: '',
    width: 0,
    height: 0,
})

export function useLightbox() {
    function open(src: string, alt: string, width: number, height: number) {
        state.src = src
        state.alt = alt
        state.width = width
        state.height = height
    }

    function close() {
        state.src = null
        state.alt = ''
    }

    return {
        state,
        open,
        close,
    }
}
