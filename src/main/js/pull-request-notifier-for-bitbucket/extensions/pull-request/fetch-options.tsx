const fetchOptions = (methodType: string) => ({
    method: methodType,
    headers: {
        'Content-Type': 'application/json',
    },
});

export default fetchOptions;